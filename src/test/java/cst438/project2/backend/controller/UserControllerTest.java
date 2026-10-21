package cst438.project2.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private static final Long USER_ID = 7L;

  @Mock
  private UserService userService;

  @Mock
  private Authentication authentication;

  private UserController userController;
  private User user;

  @BeforeEach
  void setUp() {
    userController = new UserController(userService);
    user = new User(USER_ID, "test-user", "google-subject", UserRole.USER, null, null);
  }

  @Test
  void getUsersDelegatesToService() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<UserResponse> page = new PageImpl<>(List.of(UserResponse.init(user)), pageable, 1);
    when(userService.getUsers(pageable)).thenReturn(page);

    assertEquals(page, userController.getUsers(pageable));

    verify(userService).getUsers(pageable);
  }

  @Test
  void getUserReturnsMappedResponse() {
    when(userService.getUser(USER_ID)).thenReturn(user);

    UserResponse result = userController.getUser(USER_ID);

    assertEquals(USER_ID, result.userId());
    assertEquals("test-user", result.username());
    assertEquals(UserRole.USER, result.role());
    verify(userService).getUser(USER_ID);
  }

  @Test
  void updateUserDelegatesToServiceAndMapsResponse() {
    UserRequest request = new UserRequest("updated-user", null, UserRole.ADMIN, null);
    when(userService.updateUser(USER_ID, request)).thenReturn(user);

    UserResponse result = userController.updateUser(USER_ID, request);

    assertEquals(USER_ID, result.userId());
    assertEquals(UserRole.USER, result.role());
    verify(userService).updateUser(USER_ID, request);
  }

  @Test
  void deleteUserDelegatesToService() {
    userController.deleteUser(USER_ID);

    verify(userService).deleteUser(USER_ID);
  }

  @Test
  void deleteMyAccountDelegatesToSelfDeletion() {
    userController.deleteMyAccount(authentication);

    verify(userService).deleteMe(authentication);
  }
}
