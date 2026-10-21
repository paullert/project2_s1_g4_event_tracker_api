package cst438.project2.backend.dto;


public record DeletionResponse<T>(
    String status,
    String message,
    String location,
    T object
) { }
