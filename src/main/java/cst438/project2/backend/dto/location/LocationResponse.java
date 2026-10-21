package cst438.project2.backend.dto.location;


import cst438.project2.backend.model.Location;
import cst438.project2.backend.dto.ApiDates;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationResponse(
    Long locationId,
    String locationName,
    String address,
    @JsonProperty("locationDesc") String locationDescription,
    String updatedAt
) {
  public static LocationResponse init(Location location) {
    return new LocationResponse(
        location.getLocationId(),
        location.getLocationName(),
        location.getAddress(),
        location.getLocationDescription(),
        ApiDates.format(location.getUpdatedAt() != null ? location.getUpdatedAt() : location.getCreatedAt())
    );
  }
}
