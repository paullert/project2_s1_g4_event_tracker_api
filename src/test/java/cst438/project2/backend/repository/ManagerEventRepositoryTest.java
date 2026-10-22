package cst438.project2.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import cst438.project2.backend.model.ManagerEvent;
import cst438.project2.backend.model.ManagerEventId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class ManagerEventRepositoryTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
      .withInitScript("database.sql");

  @Autowired private ManagerEventRepository ownership;
  @Autowired private EventRepository events;
  @Autowired private UserRepository users;

  @Test
  void persistsSharedOwnershipAndRemovesOnlyTheSelectedManager() {
    Long eventId = events.findAll().get(0).getId();
    Long first = users.findByUsername("manager_demo_1").orElseThrow().getUserId();
    Long second = users.findByUsername("manager_demo_2").orElseThrow().getUserId();
    ownership.saveAndFlush(new ManagerEvent(eventId, first));
    ownership.saveAndFlush(new ManagerEvent(eventId, second));
    ownership.deleteById(new ManagerEventId(eventId, first));
    ownership.flush();
    assertFalse(ownership.existsById(new ManagerEventId(eventId, first)));
    assertTrue(ownership.existsById(new ManagerEventId(eventId, second)));
    ownership.deleteForEvent(eventId);
    assertFalse(ownership.existsById(new ManagerEventId(eventId, second)));
  }
}
