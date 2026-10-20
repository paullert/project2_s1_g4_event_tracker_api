package cst438.project2.backend.dto;

import cst438.project2.backend.dto.location.LocationResponse;
import cst438.project2.backend.model.Invite;
import cst438.project2.backend.model.Location;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

//data transfer object that send following information back to user
public record InviteResponse(
        Long userId,
        Long eventId,
        String eventName,
        String eventDescription,
        LocalDateTime startsOn,
        String status,
        LocalDateTime invitedAt

) {
    public static InviteResponse init(Invite invite) {
        return new InviteResponse(
                invite.getUser().getUserId(),
                invite.getEvent().getId(),
                invite.getEvent().getEventName(),
                invite.getEvent().getEventDescription(),
                invite.getEvent().getStartsOn(),
                invite.getStatus(),
                invite.getInvitedAt()
        );
    }
}
