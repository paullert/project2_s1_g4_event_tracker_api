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

        OAuth2TokenValidator<Jwt> claims = jwt -> {
          String issuer = jwt.getClaimAsString("iss");
          String subject = jwt.getSubject();

          // Check if the token is issued by Google
          boolean isGoogle = issuer.equals("https://accounts.google.com") || issuer.equals("accounts.google.com");

          // Check if the token is intended for the client
          List<String> audiences = jwt.getAudience();
          boolean validAudience = audiences != null && audiences.contains(clientId);

          // Check if the token has a valid subject (user ID)
          boolean validSubject = subject != null && !subject.isBlank();

          // If above conditions are met, and the token has an expiration time, the token is valid
          if (isGoogle && validAudience && validSubject && jwt.getExpiresAt() != null) {
            return OAuth2TokenValidatorResult.success();
          }
          // If one or more condition's fail, the token is invalid
          return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid Google token", null));
        };

        // Add the custom validator to the decoder
        decoder.setJwtValidator(
            // Combine the default JWT validator with the custom validator
            new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                claims
            )
        );

        return decoder;
    }
}