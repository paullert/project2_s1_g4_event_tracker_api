package cst438.project2.backend.dto;

import java.time.LocalDateTime;

//data transfer object that send following information back to user
public record InviteResponse(
        Long inviteId, Long eventId,
        String eventName, String eventDescription,
        LocalDateTime startsOn, String status,
        LocalDateTime invitedAt

) {
}
