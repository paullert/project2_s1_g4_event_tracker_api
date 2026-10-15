package cst438.project2.backend.dto;


import cst438.project2.backend.model.UserRole;
//import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String username,
    UserRole role
//    LocalDateTime createdAt,
//    LocalDateTime updatedAt
) {
}
