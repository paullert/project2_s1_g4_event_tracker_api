package cst438.project2.backend.repository;

import static cst438.project2.backend.TestResources.EventValues.BASE_EVENT_DESCRIPTIONS;
import static cst438.project2.backend.TestResources.EventValues.BASE_EVENT_NAMES;
import static cst438.project2.backend.TestResources.EventValues.NOW;
import static cst438.project2.backend.TestResources.InviteValues.MESSAGES;
import static cst438.project2.backend.TestResources.LocationValues.LOCATION_IDS;
import static cst438.project2.backend.TestResources.UserValues.GOOGLE_SUBJECTS;
import static cst438.project2.backend.TestResources.UserValues.ROLES;
import static cst438.project2.backend.TestResources.UserValues.USERNAMES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.Invite;
import cst438.project2.backend.model.InviteId;
import cst438.project2.backend.model.InviteStatus;
import cst438.project2.backend.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers
public class InviteRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16")
          .withInitScript("database.sql");

  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private InviteRepository inviteRepository;
  // Uses TestResources.java to load sample values, check there for more info
  // EX: USERNAMES[0], GOOGLE_SUBJECTS[0], CREATOR_IDS[0], EVENT_LOCATION_IDS[0]

  @Test
  void savesAndFindsInvite() {
    String eventName = BASE_EVENT_NAMES[0];

    User user = new User(
        null,
        USERNAMES[1],
        GOOGLE_SUBJECTS[0],
        ROLES[0],
        NOW,
        NOW
    );
    user = userRepository.save(user);

    Event event = new Event(
        null,
        eventName,
        BASE_EVENT_DESCRIPTIONS[0],
        NOW,
        user.getUserId(),
        NOW,
        LOCATION_IDS[0]
    );
    event = eventRepository.save(event);

    Invite invite = new Invite(
        new InviteId(user.getUserId(), event.getId()),
        user,
        event,
        InviteStatus.PENDING,
        MESSAGES[0],
        NOW,
        NOW
    );

    Invite saved = inviteRepository.save(invite);

    Optional<Invite> result =
        inviteRepository.findByUser_UserIdAndEvent_Id(user.getUserId(), event.getId());

    assertTrue(result.isPresent());
    assertEquals(invite.getMessage(), result.get().getMessage());
  }

}
