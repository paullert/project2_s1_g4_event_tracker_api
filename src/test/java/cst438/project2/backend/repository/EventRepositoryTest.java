package cst438.project2.backend.repository;

import static cst438.project2.backend.TestResources.EventValues.*;
import static cst438.project2.backend.TestResources.LocationValues.*;
import static cst438.project2.backend.TestResources.UserValues.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// These tests use TestResources.java to load sample values, check there for more info

// NOTE: REQUIRES DOCKER TO BE RUNNING
@DataJpaTest
@Testcontainers
class EventRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16")
          .withInitScript("database.sql");

  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private UserRepository userRepository;
  // Uses TestResources.java to load sample values, check there for more info
  // EX: USERNAMES[0], GOOGLE_SUBJECTS[0], CREATOR_IDS[0], EVENT_LOCATION_IDS[0]

  @Test
  void savesAndFindsEvent() {
    String eventName = BASE_EVENT_NAMES[0];

    User creator = new User(
        USER_IDS[0],
        USERNAMES[0],
        GOOGLE_SUBJECTS[0],
        ROLES[0],
        NOW,
        NOW
    );
    userRepository.save(creator);

    Event event = new Event(
        null,
        eventName,
        BASE_EVENT_DESCRIPTIONS[0],
        NOW,
        USER_IDS[0],
        NOW,
        LOCATION_IDS[0]
    );

    Event saved = eventRepository.save(event);

    Optional<Event> result =
        eventRepository.findById(saved.getId());

    assertTrue(result.isPresent());
    assertEquals(eventName, result.get().getEventName());
  }
}
