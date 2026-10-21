package cst438.project2.backend.repository;

import static cst438.project2.backend.TestResources.Dates.NOW;
import static cst438.project2.backend.TestResources.UserValues.GOOGLE_SUBJECTS;
import static cst438.project2.backend.TestResources.UserValues.USERNAMES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cst438.project2.backend.model.User;

import cst438.project2.backend.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class UserRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16")
          .withDatabaseName("p2_api")
          .withUsername("postgres")
          .withPassword("postgres")
          .withInitScript("database.sql");

  @Autowired
  private UserRepository userRepository;

  @Test
  void retrievesFirstPageOfUsers() {
    Page<User> users = userRepository.findAll(
        PageRequest.of(
            0,
            20,
            Sort.by("username").ascending()
        )
    );

    assertEquals(20, users.getSize());
    assertEquals(0, users.getNumber());
  }

  @Test
  void saveAndRetrieveUser(){
    User user = new User(
        null,
        USERNAMES[0],
        GOOGLE_SUBJECTS[0],
        UserRole.USER,
        NOW,
        NOW
    );
    User saved = userRepository.save(user);
    assertEquals(USERNAMES[0], saved.getUsername());
    assertEquals(UserRole.USER, saved.getRole());

    User result = userRepository.findById(saved.getUserId()).orElseThrow(null);
    assertNotNull(result);
    assertEquals(USERNAMES[0], result.getUsername());
    assertEquals(UserRole.USER, result.getRole());
  }
}