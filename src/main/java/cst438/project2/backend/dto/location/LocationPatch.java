package cst438.project2.backend.dto.location;



public record LocationPatch(
    String locationName,
    String address,
    String locationDescription
) {
}
