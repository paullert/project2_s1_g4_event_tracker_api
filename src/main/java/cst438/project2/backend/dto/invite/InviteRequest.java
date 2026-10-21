package cst438.project2.backend.dto.invite;

public record InviteRequest(
        Long userId,
        Long eventId,
        String status,
        String message
) {

}


