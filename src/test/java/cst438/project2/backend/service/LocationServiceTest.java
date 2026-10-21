package cst438.project2.backend.service;

import static cst438.project2.backend.TestResources.Dates.NOW;
import static cst438.project2.backend.TestResources.UserValues.GOOGLE_SUBJECTS;
import static cst438.project2.backend.TestResources.UserValues.ROLES;
import static cst438.project2.backend.TestResources.UserValues.USERNAMES;
import static cst438.project2.backend.TestResources.UserValues.USER_IDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static cst438.project2.backend.TestResources.LocationValues.*;
import static cst438.project2.backend.TestResources.ErrorValues.*;

import cst438.project2.backend.dto.location.LocationCreate;
import cst438.project2.backend.dto.location.LocationPatch;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
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

  private final Long locationId = LOCATION_IDS[0];
  private final String name = LOCATION_NAMES[0];
  private final String name2 = LOCATION_NAMES[1];
  private final String address = ADDRESSES[0];
  private final String address2 = ADDRESSES[1];
  private final String description = LOCATION_DESCRIPTIONS[0];
  private final String description2 = LOCATION_DESCRIPTIONS[1];
  private final LocalDateTime now = NOW;
  private final LocalDateTime createdAt = now;
  private final LocalDateTime updatedAt = now;

  User creator = new User(
      USER_IDS[0],
      USERNAMES[0],
      GOOGLE_SUBJECTS[0],
      ROLES[0],
      NOW,
      NOW
  );

  @BeforeEach
  void setUp() {
    location = new Location(
        locationId,
        name,
        address,
        description,
        creator,
        createdAt,
        updatedAt
    );
    location.setLocationId(locationId);
    location.setLocationName(name);
    location.setAddress(address);
    location.setLocationDescription(description);
    location.setUpdatedAt(updatedAt);
  }

  @Test
  void putLocationReplacesAllFields() {
    LocationCreate request = new LocationCreate(
        name2,
        address2,
        description2
    );

    when(locationRepository.save(location)).thenReturn(location);

    Location result = locationService.putLocation(location, request);

    assertEquals(name2, result.getLocationName());
    assertEquals(address2, result.getAddress());
    assertEquals(description2, result.getLocationDescription());

    verify(locationRepository).save(location);
  }

  @Test
  void patchLocationReplacesOnlySuppliedFields() {
    String updateName1 = PATCH_LOCATION_NAMES[0];
    String updateName2 = PATCH_LOCATION_NAMES[1];

    LocationPatch request1 = new LocationPatch(
        updateName1,
        address2,
        null
    );

    LocationPatch request2 = new LocationPatch(
        updateName2,
        null,
        description2
    );

    when(locationRepository.save(location)).thenReturn(location);

    Location result1 = locationService.patchLocation(location, request1);
    assertEquals(updateName1, result1.getLocationName());
    assertEquals(address2, result1.getAddress());
    assertEquals(this.description2, result1.getLocationDescription());

    Location result2 = locationService.patchLocation(location, request2);
    assertEquals(updateName2, result2.getLocationName());
    assertEquals(address2, result2.getAddress());
    assertEquals(description2, result2.getLocationDescription());

    verify(locationRepository, times(2)).save(location);
  }

  @Test
  void missingLocationNameThrowsNotFound() {
    // All not found requests should throw NotFoundException with a message like:
    String notFoundMsg = constructNotFoundMessage("Location", "id "+ INVALID_REQUEST_ID);
    when(locationRepository.findById(INVALID_REQUEST_ID))
        .thenReturn(java.util.Optional.empty());

    NotFoundException ex = assertThrows(
        NotFoundException.class,
        () -> locationService.getLocationById(INVALID_REQUEST_ID)
    );

    assertEquals(notFoundMsg, ex.getMessage());
    verify(locationRepository, never()).save(any());
  }

  @Test
  void incompletePutThrowsBadRequest() {
    // All bad requests should throw BadRequestException with the following message
    String badReqMsg = constructBadRequestMessage("locationName, address, locationDescription");

    LocationCreate request1 = new LocationCreate(
        null,
        INVALID_REQUEST_STRING,
        INVALID_REQUEST_STRING
    );

    LocationCreate request2 = new LocationCreate(
        null,
        null,
        INVALID_REQUEST_STRING
    );

    LocationCreate request3 = new LocationCreate(
        null,
        null,
        null
    );

    LocationCreate request4 = new LocationCreate(
        INVALID_REQUEST_STRING,
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
    assertEquals(name, location.getLocationName());
    assertEquals(address, location.getAddress());
    assertEquals(description, location.getLocationDescription());

    // Verify no database calls were made
    verify(locationRepository, never()).save(any());
  }
}
