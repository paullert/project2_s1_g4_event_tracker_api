package cst438.project2.backend.controller;

import cst438.project2.backend.dto.user.RegisterRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.model.User;
import cst438.project2.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import cst438.project2.backend.dto.auth.GoogleCodeExchangeRequest;
import cst438.project2.backend.dto.auth.GoogleTokenResponse;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class OauthController {

  UserService userService;
  private final RestClient googleClient;
  private final String googleClientId;
  private final String configuredRedirectUri;

  public OauthController(
          @Value("${google.oauth2.client-id}") String googleClientId,
          @Value("${google.oauth2.redirect-uri}") String configuredRedirectUri
  ) {
    this.googleClient = RestClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build();

    this.googleClientId = googleClientId;
    this.configuredRedirectUri = configuredRedirectUri;
  }

  @GetMapping("/me")
  public UserResponse getCurrentUser(
      Authentication authentication
  ) {
    return UserResponse.init(userService.findUserFromAuthentication(authentication));
  }

  @PostMapping("/register")
  public UserResponse register(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody RegisterRequest request
  ) {
    User user = userService.registerUser(
        jwt,
        request.username()
    );

    return UserResponse.init(user);
  }
}
