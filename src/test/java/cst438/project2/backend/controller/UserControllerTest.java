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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Request/response tests; UserAuthorizationTest exercises the real secured service.
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
    mvc.perform(get("/api/v1/users?page=1&size=2&sort=username,desc").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].userId").value(7))
        .andExpect(jsonPath("$.content[0].role").value("USER"))
        .andExpect(jsonPath("$.page.totalElements").value(3));
    verify(users).getUsers(pageable);
  }

  @Test
  void createUserBindsAllFieldsAndReturns201() throws Exception {
    var request = new UserRequest("alex", "alex@example.com", UserRole.USER, "google-subject");
    when(users.createUser(request)).thenReturn(UserResponse.init(user));
    mvc.perform(post("/api/v1/users").with(jwt()).contentType(MediaType.APPLICATION_JSON)
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
    mvc.perform(get("/api/v1/users/7").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("alex"));
    verify(users).getUser(7L);
  }

  @Test
  void patchUserBindsPathAndBody() throws Exception {
    var request = new UserRequest("alex", "alex@example.com", UserRole.MANAGER, "google-subject");
    user.setRole(UserRole.MANAGER);
    when(users.updateUser(7L, request)).thenReturn(user);
    mvc.perform(patch("/api/v1/users/7").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"username":"alex","email":"alex@example.com","role":"MANAGER","subject":"google-subject"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("MANAGER"));
    verify(users).updateUser(7L, request);
  }

  @Test
  void missingUserReturns404() throws Exception {
    when(users.getUser(99L)).thenThrow(new NotFoundException(99L, "User"));
    mvc.perform(get("/api/v1/users/99").with(jwt())).andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"));
  }

  @Test
  void nonNumericIdReturns400BeforeCallingService() throws Exception {
    mvc.perform(get("/api/v1/users/not-a-number").with(jwt())).andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400));
    verifyNoInteractions(users);
  }

  @Test
  void invalidRoleIsRejectedDuringJsonBinding() throws Exception {
    mvc.perform(post("/api/v1/users").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"alex\",\"role\":\"SUPERUSER\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(users);
  }

}
