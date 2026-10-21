package cst438.project2.backend.dto.event;

import cst438.project2.backend.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record EventResponse(
        Long eventId,
        String eventName,
        String eventDescription,
        String startsOn,
        Long creatorId,
        Long locationId
        ) {

    public static EventResponse init(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getEventDescription(),
                event.getStartsOn()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),  // Desired format, was ISO format
                event.getCreatorId(),
                event.getLocationId()
        );
    }
}
