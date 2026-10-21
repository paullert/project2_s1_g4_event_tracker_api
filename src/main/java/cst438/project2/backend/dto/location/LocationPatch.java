package cst438.project2.backend.dto.location;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;



public record LocationPatch(
    String locationName,
    String address,
    @JsonProperty("locationDesc") @JsonAlias("locationDescription") String locationDescription
) {
}
