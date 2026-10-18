package cst438.project2.backend.dto.location;

public record LocationCreateRequest(
    String locationName,
    String address,
    String locationDescription
) {

}
