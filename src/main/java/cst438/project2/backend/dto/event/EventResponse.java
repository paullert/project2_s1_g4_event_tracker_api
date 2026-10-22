package cst438.project2.backend.dto.event;

import cst438.project2.backend.model.Event;
import cst438.project2.backend.dto.ApiDates;

public record EventResponse(
        Long eventId,
        String eventName,
        String eventDescription,
        String startsOn,
        Long creatorId,
        Long locationId,
        String updatedAt
        ) {

    public static EventResponse init(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getEventDescription(),
                ApiDates.format(event.getStartsOn()),
                event.getCreatorId(),
                event.getLocationId(),
                ApiDates.format(event.getUpdatedAt() != null ? event.getUpdatedAt() : event.getCreatedAt())
        );
    }
}
