package cst438.project2.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

// Mapping/serialization tests; AuthTest and SelfDeletionSecurityTest use real secured services.
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private UserService users;
  @MockitoBean private JwtAuthConverter converter;
  @MockitoBean private JwtDecoder decoder;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User(7L, "alex", "google-subject", UserRole.USER, null, null);
    user.setEmail("alex@example.com");
  }

  @Test
  void listUsersBindsPaginationAndSort() throws Exception {
    var pageable = PageRequest.of(1, 2, Sort.by("username").descending());
    when(users.getUsers(pageable)).thenReturn(
        new PageImpl<>(List.of(UserResponse.init(user)), pageable, 3));
    mvc.perform(get("/users?page=1&size=2&sort=username,desc").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].userId").value(7))
        .andExpect(jsonPath("$.content[0].role").value("USER"))
        .andExpect(jsonPath("$.totalElements").value(3));
    verify(users).getUsers(pageable);
  }

  @Test
  void createUserBindsAllFieldsAndReturns201() throws Exception {
    var request = new UserRequest("alex", "alex@example.com", UserRole.USER, "google-subject");
    when(users.createUser(request)).thenReturn(UserResponse.init(user));
    mvc.perform(post("/users").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"username":"alex","email":"alex@example.com","role":"USER","subject":"google-subject"}
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value(7))
        .andExpect(jsonPath("$.email").value("alex@example.com"));
    verify(users).createUser(request);
  }

  @Test
  void getUserReturnsJson() throws Exception {
    when(users.getUser(7L)).thenReturn(user);
    mvc.perform(get("/users/7").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("alex"));
    verify(users).getUser(7L);
  }

  @Test
  void patchUserBindsPathAndBody() throws Exception {
    var request = new UserRequest("alex", "alex@example.com", UserRole.MANAGER, "google-subject");
    user.setRole(UserRole.MANAGER);
    when(users.updateUser(7L, request)).thenReturn(user);
    mvc.perform(patch("/users/7").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"username":"alex","email":"alex@example.com","role":"MANAGER","subject":"google-subject"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("MANAGER"));
    verify(users).updateUser(7L, request);
  }

  @Test
  void deleteUserReturns204WithoutBody() throws Exception {
    mvc.perform(delete("/users/7").with(jwt()))
        .andExpect(status().isNoContent()).andExpect(content().string(""));
    verify(users).deleteUser(7L);
  }

  @Test
  void deleteMeUsesAuthenticatedIdentityInsteadOfNumericUserRoute() throws Exception {
    mvc.perform(delete("/users/me").with(jwt().jwt(token -> token.subject("self-subject"))))
        .andExpect(status().isNoContent());
    verify(users).deleteMe(argThat(auth -> "self-subject".equals(auth.getName())));
    verify(users, never()).deleteUser(any());
  }

  @ParameterizedTest
  @CsvSource({"GET,/users", "POST,/users", "GET,/users/7", "PATCH,/users/7",
      "DELETE,/users/7", "DELETE,/users/me"})
  void protectedRoutesRequireAuthentication(String method, String path) throws Exception {
    mvc.perform(request(HttpMethod.valueOf(method), path)
        .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(users);
  }

  @Test
  void missingUserReturns404() throws Exception {
    when(users.getUser(99L)).thenThrow(new NotFoundException(99L, "User"));
    mvc.perform(get("/users/99").with(jwt())).andExpect(status().isNotFound());
  }

  @Test
  void nonNumericIdReturns400BeforeCallingService() throws Exception {
    mvc.perform(get("/users/not-a-number").with(jwt())).andExpect(status().isBadRequest());
    verifyNoInteractions(users);
  }

  @Test
  void invalidRoleIsRejectedDuringJsonBinding() throws Exception {
    mvc.perform(post("/users").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"alex\",\"role\":\"SUPERUSER\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(users);
  }

  @Test
  void malformedJsonReturns400() throws Exception {
    mvc.perform(post("/users").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{broken"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(users);
  }

  @Test
  void duplicateUserConflictIsPreserved() throws Exception {
    when(users.createUser(any())).thenThrow(
        new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));
    mvc.perform(post("/users").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"alex\",\"role\":\"USER\"}"))
        .andExpect(status().isConflict());
  }
}
