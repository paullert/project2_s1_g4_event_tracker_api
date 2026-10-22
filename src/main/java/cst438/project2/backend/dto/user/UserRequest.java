package cst438.project2.backend.dto.user;

import cst438.project2.backend.model.UserRole;

/** Fields an administrator may change on a user account. */
public record UserRequest(
    String username,
    String email,
    UserRole role,
    String subject
) {

}
