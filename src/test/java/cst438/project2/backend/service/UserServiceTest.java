package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final Long USER_ID = 7L;
  private static final String GOOGLE_SUBJECT = "google-subject";

  @Mock
  private UserRepository userRepository;

  private UserService userService;
  private User user;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository);
    user = new User(USER_ID, "test-user", GOOGLE_SUBJECT, UserRole.USER, null, null);
  }

  @Test
  void updateUserChangesRoleAndSavesUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    User result = userService.updateUser(USER_ID, new UserRequest(null, null, UserRole.ADMIN, null));

    assertEquals(UserRole.ADMIN, result.getRole());
    verify(userRepository).save(user);
  }

  @Test
  void updateUserRejectsAnEmptyPatch() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> userService.updateUser(USER_ID, new UserRequest(null, null, null, null)));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void findUserFromAuthenticationRejectsNonJwtAuthentication() {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("user", "password");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.findUserFromAuthentication(authentication));

    assertEquals("Authentication is not a JWT", exception.getMessage());
    verify(userRepository, never()).findByGoogleSubject(any());
  }

  @Test
  void registrationUsesTheVerifiedGoogleEmailAndAlwaysCreatesAnOrdinaryUser() {
    Jwt identity = Jwt.withTokenValue("identity").header("alg", "RS256")
        .subject(GOOGLE_SUBJECT).claim("email", "alex@example.com").claim("role", "ADMIN").build();
    when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    User registered = userService.registerUser(identity, " alex ");
    assertEquals("alex", registered.getUsername());
    assertEquals("alex@example.com", registered.getEmail());
    assertEquals(GOOGLE_SUBJECT, registered.getGoogleSubject());
    assertEquals(UserRole.USER, registered.getRole());
  }

}
