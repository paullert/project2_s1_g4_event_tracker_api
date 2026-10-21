package cst438.project2.backend.dto.event;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cst438.project2.backend.dto.ApiDates;

public record EventRequest(
        String eventName,
        String eventDescription,
        LocalDateTime startsOn,
        Long creatorId,
        Long locationId
        ) {
  @JsonCreator
  public static EventRequest fromJson(
      @JsonProperty("eventName") String name,
      @JsonProperty("eventDescription") String description,
      @JsonProperty("startsOn") String startsOn,
      @JsonProperty("creatorId") Long creatorId,
      @JsonProperty("locationId") Long locationId) {
    return new EventRequest(name, description, ApiDates.parse(startsOn), creatorId, locationId);
  }
}
