package cst438.project2.backend.dto.auth;

public record GoogleCodeExchangeRequest(
        String code,
        String codeVerifier,
        String redirectUri
) {}