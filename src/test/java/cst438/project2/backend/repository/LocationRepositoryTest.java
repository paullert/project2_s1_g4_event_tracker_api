package cst438.project2.backend.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    User creator = new User();
    creator.setUsername("testuser");
    creator.setGoogleSubject("1234567890");

    creator = userRepository.save(creator);

    Location location = new Location(
        "Main Office",
        "10 Main Street",
        "Primary office",
        creator
    );

    Location saved = locationRepository.save(location);

    Optional<Location> result =
        locationRepository.findById(saved.getLocationId());

    assertTrue(result.isPresent());
    assertEquals("Main Office", result.get().getLocationName());
  }
}
