package cst438.project2.backend.dto;


public record CreationResponse<T>(
    String status,
    String message,
    String location,
    T object
) { }
