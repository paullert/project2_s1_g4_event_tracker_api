package cst438.project2.backend.controller;

import cst438.project2.backend.dto.auth.GoogleTokenResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class GoogleOAuthController {

    private final RestClient googleClient;
    private final String googleClientId;
    private final String configuredRedirectUri;

    private final Map<String, String> pendingLogins =
            new ConcurrentHashMap<>();

    public GoogleOAuthController(
            @Value("${google.oauth2.client-id}") String googleClientId,
            @Value("${google.oauth2.redirect-uri}") String configuredRedirectUri
    ) {
        this.googleClient =
                RestClient.builder()
                        .baseUrl("https://oauth2.googleapis.com")
                        .build();

        this.googleClientId = googleClientId;
        this.configuredRedirectUri = configuredRedirectUri;
    }
    @GetMapping("/oauth2/start")
    public ResponseEntity<Void> startGoogleLogin() {
        String state = randomUrlSafeString(32);
        String codeVerifier = randomUrlSafeString(64);
        String codeChallenge = createCodeChallenge(codeVerifier);

        pendingLogins.put(state, codeVerifier);

        URI googleLoginUrl = UriComponentsBuilder
                .fromUriString(
                        "https://accounts.google.com/o/oauth2/v2/auth"
                )
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", configuredRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("prompt", "select_account")
                .build()
                .encode()
                .toUri();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(googleLoginUrl)
                .build();
    }

    @GetMapping("/oauth2redirect")
    public ResponseEntity<Void> googleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        String codeVerifier = pendingLogins.remove(state);

        if (codeVerifier == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired OAuth state"
            );
        }

        GoogleTokenResponse googleTokens =
                googleClient.post()
                        .uri("/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(createGoogleTokenRequest(code, codeVerifier))
                        .retrieve()
                        .body(GoogleTokenResponse.class);

        if (googleTokens == null || googleTokens.idToken() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Google token exchange failed"
            );
        }

        String ticket = createLoginTicket(googleTokens);

        URI androidCallback = URI.create(
                "https://project2-s1-g4-event-tracker-api.onrender.com" +
                        "/auth/app-callback?ticket=" + ticket
        );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(androidCallback)
                .build();
    }

    private MultiValueMap<String, String> createGoogleTokenRequest(
            String code,
            String codeVerifier
    ) {
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("client_id", googleClientId);
        form.add("code", code);
        form.add("code_verifier", codeVerifier);
        form.add("redirect_uri", configuredRedirectUri);
        form.add("grant_type", "authorization_code");

        return form;
    }

    private String createLoginTicket(
            GoogleTokenResponse googleTokens
    ) {
        return UUID.randomUUID().toString();
    }

    private String randomUrlSafeString(int byteCount) {
        byte[] bytes = new byte[byteCount];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String createCodeChallenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(
                            StandardCharsets.US_ASCII
                    ));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(digest);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }
}