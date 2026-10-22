package cst438.project2.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import cst438.project2.backend.model.*;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// Shared PostgreSQL setup for basic entity mappings; custom queries have their own tests.
@DataJpaTest
@Testcontainers
class EntityPersistenceTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
      .withInitScript("database.sql");

  @Autowired private UserRepository users;
  @Autowired private LocationRepository locations;
  @Autowired private EventRepository events;
  @Autowired private InviteRepository invites;
  @Autowired private EntityManager entities;

  @Test
  void userRoundTripPreservesIdentityAndRole() {
    String suffix = UUID.randomUUID().toString();
    User user = new User(null, "user-" + suffix, "subject-" + suffix, UserRole.USER, null, null);
    user.setEmail(suffix + "@example.com");
    users.saveAndFlush(user);
    entities.clear();

    User loaded = users.findByGoogleSubject(user.getGoogleSubject()).orElseThrow();
    assertEquals(user.getUserId(), loaded.getUserId());
    assertEquals(user.getEmail(), loaded.getEmail());
    assertEquals(UserRole.USER, loaded.getRole());
    assertEquals(loaded.getUserId(), users.findByUsername(user.getUsername()).orElseThrow().getUserId());
  }

  @Test
  void locationRoundTripPreservesItsCreatorAndAddress() {
    User creator = users.findByUsername("manager_demo_1").orElseThrow();
    Location location = locations.saveAndFlush(new Location(null, "Library", "100 Main St",
        "Meeting room", creator, null, null));
    entities.clear();

    Location loaded = locations.findById(location.getLocationId()).orElseThrow();
    assertEquals("Library", loaded.getLocationName());
    assertEquals("100 Main St", loaded.getAddress());
    assertEquals("Meeting room", loaded.getLocationDescription());
    assertEquals(creator.getUserId(), loaded.getCreator().getUserId());
  }

  @Test
  void eventAndInviteRoundTripPreservesTheCompositeKeyAndUserFilter() {
    User recipient = users.findByUsername("ann_demo").orElseThrow();
    Long locationId = locations.findAll().get(0).getLocationId();
    LocalDateTime start = LocalDateTime.of(2026, 11, 1, 12, 0);
    Event event = events.saveAndFlush(new Event(null, "Study session", "Description", start,
        recipient.getUserId(), null, locationId));
    InviteId id = new InviteId(recipient.getUserId(), event.getId());
    invites.saveAndFlush(new Invite(id, recipient, event, InviteStatus.PENDING, "Welcome", start, null));
    entities.clear();

    Invite loaded = invites.findById(new InviteId(recipient.getUserId(), event.getId())).orElseThrow();
    assertEquals(id, loaded.getInviteId());
    assertEquals("Study session", loaded.getEvent().getEventName());
    assertEquals(start, loaded.getEvent().getStartsOn());
    assertEquals(InviteStatus.PENDING, loaded.getStatus());
    assertEquals("Welcome", loaded.getMessage());
    var page = invites.findByUser_UserId(recipient.getUserId(), PageRequest.of(0, 20));
    assertTrue(page.stream().allMatch(invite -> recipient.getUserId().equals(invite.getUser().getUserId())));
    assertTrue(page.stream().anyMatch(invite -> event.getId().equals(invite.getEvent().getId())));
  }
}
