package cst438.project2.backend.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static cst438.project2.backend.TestResources.LocationValues.*;
import static cst438.project2.backend.TestResources.UserValues.*;

import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;


// NOTE: REQUIRES DOCKER TO BE RUNNING
@DataJpaTest
@Testcontainers
class LocationRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16")
          .withInitScript("database.sql");

  @Autowired
  private LocationRepository locationRepository;
  @Autowired
  private UserRepository userRepository;

  @Test
  void savesAndFindsLocation() {
    String locationName = LOCATION_NAMES[0];
    String address = ADDRESSES[0];
    String description = LOCATION_DESCRIPTIONS[0];

    User creator = new User();
    creator.setUsername(USERNAMES[0]);
    creator.setGoogleSubject(GOOGLE_SUBJECTS[0]);

    creator = userRepository.save(creator);

    Location location = new Location(
        null,
        locationName,
        address,
        description,
        creator,
        null,
        null
    );

    Location saved = locationRepository.save(location);

    Optional<Location> result =
        locationRepository.findById(saved.getLocationId());

    assertTrue(result.isPresent());
    assertEquals(locationName, result.get().getLocationName());
  }
}
