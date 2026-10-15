// NOTE: REQUIRES DOCKER TO BE RUNNING

package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cst438.project2.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class UserServiceTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16")
          .withInitScript("database.sql");

  @Autowired
  private UserService userService;

  @Test
  void retrievesTwentyUsersPerPage() {
    Page<User> result = userService.getUsers(0);

    assertEquals(20, result.getSize());
    assertEquals(0, result.getNumber());
  }
}