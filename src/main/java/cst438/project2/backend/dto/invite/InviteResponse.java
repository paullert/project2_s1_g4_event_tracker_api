package cst438.project2.backend.dto.invite;

import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.model.Invite;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//data transfer object that send following information back to user
public record InviteResponse(
        Long userId,
        Long eventId,
        String eventName,
        String eventDescription,
        String startsOn,
        String status,
        LocalDateTime invitedAt

) {
  public static InviteResponse init(Invite invite) {
    return new InviteResponse(
        invite.getInviteId().getUserId(),
        invite.getInviteId().getEventId(),
        invite.getEvent().getEventName(),
        invite.getEvent().getEventDescription(),
        invite.getEvent().getStartsOn()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        invite.getStatus().toString(),
        invite.getInvitedAt()
    );
  }
}
