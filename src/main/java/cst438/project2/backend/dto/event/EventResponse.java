package cst438.project2.backend.dto.event;

import cst438.project2.backend.model.Event;

import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        String eventName,
        String eventDescription,
        LocalDateTime startsOn,
        Long creatorId,
        Long locationId
        ) {

    public static EventResponse init(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getEventDescription(),
                event.getStartsOn(),
                event.getCreatorId(),
                event.getLocationId()
        );
    }
}
