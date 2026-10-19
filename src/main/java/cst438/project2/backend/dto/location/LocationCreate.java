package cst438.project2.backend.dto.location;

public record LocationCreate(
    String locationName,
    String address,
    String locationDescription
) {

}
