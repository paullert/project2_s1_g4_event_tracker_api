package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cst438.project2.backend.model.User;

import cst438.project2.backend.repository.UserRepository;
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
class UserRepoTest {

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
}