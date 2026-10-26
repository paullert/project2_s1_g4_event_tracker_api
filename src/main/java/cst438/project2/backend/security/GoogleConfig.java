package cst438.project2.backend.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class GoogleConfig {

    @Bean("googleJwtDecoder")
    JwtDecoder googleJwtDecoder(
        @Value("${google.oauth2.client-id}")
        String clientId
    ) {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withJwkSetUri(
                "https://www.googleapis.com/oauth2/v3/certs"
            ).build();

        decoder.setJwtValidator(tokenValidator(clientId));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> tokenValidator(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("google.oauth2.client-id must identify the Android OAuth client");
        }
        OAuth2TokenValidator<Jwt> claims = jwt -> {
          String issuer = jwt.getClaimAsString("iss");
          String subject = jwt.getSubject();

          // Check if the token is issued by Google
          boolean isGoogle = "https://accounts.google.com".equals(issuer)
              || "accounts.google.com".equals(issuer);

          // Check if the token is intended for the client
          List<String> audiences = jwt.getAudience();
          boolean validAudience = audiences != null && audiences.contains(clientId);
          String authorizedParty = jwt.getClaimAsString("azp");
          boolean validAuthorizedParty = clientId.equals(authorizedParty)
              || (authorizedParty == null && audiences != null && audiences.size() == 1);

          // Check if the token has a valid subject (user ID)
          boolean validSubject = subject != null && !subject.isBlank();

          // If above conditions are met, and the token has an expiration time, the token is valid
          if (isGoogle && validAudience && validAuthorizedParty && validSubject
              && jwt.getExpiresAt() != null) {
            return OAuth2TokenValidatorResult.success();
          }
          // If one or more condition's fail, the token is invalid
          return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid Google token", null));
        };

        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                claims
        );
    }
}
