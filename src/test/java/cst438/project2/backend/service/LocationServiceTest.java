package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cst438.project2.backend.dto.location.LocationCreate;
import cst438.project2.backend.dto.location.LocationPatch;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.repository.LocationRepository;
import cst438.project2.backend.security.SecurityConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class LocationServiceTest {

  @Mock
  private LocationRepository locationRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private LocationService locationService;

  private Location location;

  private final Long locationId = 13L;

  @BeforeEach
  void setUp() {
    location = new Location();
    location.setLocationId(locationId);
    location.setLocationName("Side Office");
    location.setAddress("10 Side Street");
    location.setLocationDescription("Secondary office");
    location.setUpdatedAt(LocalDateTime.parse("2026-01-01T12:00:00"));
  }

  @Test
  void putLocationReplacesAllFields() {
    String name = "Updated Side Office";
    String address = "20 Side Street";
    String description = "Better side office description";
    LocationCreate request = new LocationCreate(
        name,
        address,
        description
    );

    when(locationRepository.save(location)).thenReturn(location);

    Location result = locationService.putLocation(location, request);

    assertEquals(name, result.getLocationName());
    assertEquals(address, result.getAddress());
    assertEquals(description, result.getLocationDescription());

    verify(locationRepository).save(location);
  }

  @Test
  void patchLocationReplacesOnlySuppliedFields() {
    String name1 = "Partially Updated Side Office";
    String name2 = "Fully Updated Side Office";
    String address = "20 Side Street";
    String description = "Better side office description";

    LocationPatch request1 = new LocationPatch(
        name1,
        address,
        null
    );

    LocationPatch request2 = new LocationPatch(
        name2,
        null,
        description
    );

    when(locationRepository.save(location)).thenReturn(location);

    Location result1 = locationService.patchLocation(location, request1);
    assertEquals(name1, result1.getLocationName());
    assertEquals(address, result1.getAddress());
    assertEquals("Secondary office", result1.getLocationDescription());

    Location result2 = locationService.patchLocation(location, request2);
    assertEquals(name2, result2.getLocationName());
    assertEquals(address, result2.getAddress());
    assertEquals(description, result2.getLocationDescription());

    verify(locationRepository, times(2)).save(location);
  }

  @Test
  void missingLocationNameThrowsNotFound() {
    when(locationRepository.findById(999L))
        .thenReturn(java.util.Optional.empty());

    NotFoundException ex = assertThrows(
        NotFoundException.class,
        () -> locationService.getLocationById(999L)
    );

    assertEquals("Location with id 999 not found", ex.getMessage());
    verify(locationRepository, never()).save(any());
  }

  @Test
  void incompletePutThrowsBadRequest() {
    // All bad requests should throw BadRequestException with the following message
    String badReqMsg = "Missing 1 or more required fields: locationName, address, locationDescription";

    LocationCreate request1 = new LocationCreate(
        null,
        "ERR",
        "ERR"
    );

    LocationCreate request2 = new LocationCreate(
        null,
        null,
        "ERR"
    );

    LocationCreate request3 = new LocationCreate(
        null,
        null,
        null
    );

    LocationCreate request4 = new LocationCreate(
        "ERR",
        null,
        null
    );

    BadRequestException ex1 = assertThrows(
        BadRequestException.class,
        () -> locationService.putLocation(location, request1)
    );
    assertEquals(badReqMsg, ex1.getMessage());

    BadRequestException ex2 = assertThrows(
        BadRequestException.class,
        () -> locationService.putLocation(location, request2)
    );
    assertEquals(badReqMsg, ex2.getMessage());

    BadRequestException ex3 = assertThrows(
        BadRequestException.class,
        () -> locationService.putLocation(location, request3)
    );
    assertEquals(badReqMsg, ex3.getMessage());

    BadRequestException ex4 = assertThrows(
        BadRequestException.class,
        () -> locationService.putLocation(location, request4)
    );
    assertEquals(badReqMsg, ex4.getMessage());

    // Verify no fields changed during the failed puts
    assertEquals("Side Office", location.getLocationName());
    assertEquals("10 Side Street", location.getAddress());
    assertEquals("Secondary office", location.getLocationDescription());

    // Verify no database calls were made
    verify(locationRepository, never()).save(any());
  }
}
