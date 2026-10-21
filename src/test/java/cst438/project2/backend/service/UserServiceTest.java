package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
  void getUsersMapsUsersToResponses() {
    Pageable pageable = PageRequest.of(0, 10);
    when(userRepository.findAll(pageable))
        .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

    Page<UserResponse> result = userService.getUsers(pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals(USER_ID, result.getContent().get(0).userId());
    assertEquals("test-user", result.getContent().get(0).username());
    assertEquals(UserRole.USER, result.getContent().get(0).role());
    verify(userRepository).findAll(pageable);
  }

  @Test
  void getUserReturnsUserWhenFound() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    assertEquals(user, userService.getUser(USER_ID));
  }

  @Test
  void getUserThrowsWhenUserIsMissing() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(
        NotFoundException.class, () -> userService.getUser(USER_ID));

    assertEquals("User with id 7 not found", exception.getMessage());
  }

  @Test
  void updateUserChangesRoleAndSavesUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    User result = userService.updateUser(USER_ID, new UserRequest(null, null, UserRole.ADMIN));

    assertEquals(UserRole.ADMIN, result.getRole());
    verify(userRepository).save(user);
  }

  @Test
  void updateUserRequiresRole() {
    assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(USER_ID, new UserRequest(null, null, null)));

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void deleteUserFindsAndDeletesUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    userService.deleteUser(USER_ID);

    verify(userRepository).delete(user);
  }

  @Test
  void findUserFromAuthenticationReturnsMatchingUser() {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject(GOOGLE_SUBJECT)
        .claim("scope", "openid")
        .build();
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
    when(userRepository.findByGoogleSubject(GOOGLE_SUBJECT)).thenReturn(Optional.of(user));

    assertEquals(user, userService.findUserFromAuthentication(authentication));
    verify(userRepository).findByGoogleSubject(GOOGLE_SUBJECT);
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
  void findUserFromAuthenticationReturnsNotFoundWhenSubjectIsUnknown() {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject("unknown-subject")
        .build();
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
    when(userRepository.findByGoogleSubject("unknown-subject")).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> userService.findUserFromAuthentication(authentication));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }
}
