package cst438.project2.backend.dto.event;

import java.time.LocalDateTime;

public record EventRequest(
        String eventName,
        String eventDescription,
        LocalDateTime startsOn,
        Long creatorId,
        Long locationId
        ) { }
