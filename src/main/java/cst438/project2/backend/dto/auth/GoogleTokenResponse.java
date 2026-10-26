package cst438.project2.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("access_token")
        String accessToken,

        String email,
        String name
) {}
