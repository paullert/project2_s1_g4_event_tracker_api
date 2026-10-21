package cst438.project2.backend.dto.invite;

import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.model.Invite;

import java.time.LocalDateTime;
import cst438.project2.backend.dto.ApiDates;

//data transfer object that send following information back to user
public record InviteResponse(
        Long userId,
        Long eventId,
        String eventName,
        String eventDescription,
        String startsOn,
        String status,
        String invitedAt,
        String message,
        String respondedAt

) {
  public static InviteResponse init(Invite invite) {
    return new InviteResponse(
        invite.getInviteId().getUserId(),
        invite.getInviteId().getEventId(),
        invite.getEvent().getEventName(),
        invite.getEvent().getEventDescription(),
        ApiDates.format(invite.getEvent().getStartsOn()),
        invite.getStatus().toString(),
        ApiDates.format(invite.getInvitedAt()),
        invite.getMessage(),
        ApiDates.format(invite.getRespondedAt())
    );
  }
}
