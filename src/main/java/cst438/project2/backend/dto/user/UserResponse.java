package cst438.project2.backend.dto.user;


import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.dto.ApiDates;

public record UserResponse(
    Long userId,
    String username,
    String email,
    String subject,
    UserRole role,
    String createdAt,
    String updatedAt
) {
  public static UserResponse init(User user) {
    return new UserResponse(
        user.getUserId(),
        user.getUsername(),
        user.getEmail(),
        user.getGoogleSubject(),
        user.getRole(),
        ApiDates.format(user.getCreatedAt()),
        ApiDates.format(user.getUpdatedAt())
    );
  }
}
