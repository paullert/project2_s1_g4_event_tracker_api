package cst438.project2.backend.dto.user;


import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String username,
    String email,
    String subject,
    UserRole role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
  public static UserResponse init(User user) {
    return new UserResponse(
        user.getUserId(),
        user.getUsername(),
        user.getEmail(),
        user.getGoogleSubject(),
        user.getRole(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }
}
