package cst438.project2.backend.dto.location;


import cst438.project2.backend.model.Location;
import java.time.LocalDateTime;

public record LocationResponse(
    Long locationId,
    String locationName,
    String address,
    String locationDescription,
    LocalDateTime updatedAt
) {
  public static LocationResponse init(Location location) {
    return new LocationResponse(
        location.getLocationId(),
        location.getLocationName(),
        location.getAddress(),
        location.getLocationDescription(),
        location.getUpdatedAt()
    );
  }
}
