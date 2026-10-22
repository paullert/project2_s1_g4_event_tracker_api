package cst438.project2.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.UserRepository;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserService.class})
class UserAuthorizationTest {
  private static final String SUBJECT = "signed-in-google-subject";
  private static final Long USER_ID = 7L;

  @Autowired private MockMvc mvc;
  @MockitoBean private UserRepository users;
  @MockitoBean private JwtAuthConverter converter;
  @MockitoBean private JwtDecoder decoder;


  @ParameterizedTest
  @EnumSource(UserRole.class)
  void registeredUserCanDeleteOnlyTheirOwnAccount(UserRole role) throws Exception {
    User user = new User(USER_ID, "test-user", SUBJECT, role, null, null);
    when(users.findByGoogleSubject(SUBJECT)).thenReturn(Optional.of(user));

    mvc.perform(delete("/api/v1/users/me").queryParam("userId", "999")
        .with(jwt().jwt(token -> token.subject(SUBJECT))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role.name()))))
        .andExpect(status().isNoContent());

    verify(users).delete(user);
    verify(users, never()).findById(any());
  }

  @Test
  void selfDeletionRequiresAuthentication() throws Exception {
    mvc.perform(delete("/api/v1/users/me"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
    verifyNoInteractions(users);
  }

  @Test
  void unregisteredGoogleUserCannotDeleteAnAccount() throws Exception {
    mvc.perform(delete("/api/v1/users/me")
        .with(jwt().jwt(token -> token.subject(SUBJECT)).authorities(List.of())))
        .andExpect(status().isForbidden());
    verifyNoInteractions(users);
  }

  @Test
  void ordinaryUserCannotDeleteAnotherAccountById() throws Exception {
    mvc.perform(delete("/api/v1/users/999")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
    verifyNoInteractions(users);
  }

  @Test
  void administratorCanStillDeleteAnAccountById() throws Exception {
    User user = new User(USER_ID, "target-user", "target-subject", UserRole.USER, null, null);
    when(users.findById(USER_ID)).thenReturn(Optional.of(user));

    mvc.perform(delete("/api/v1/users/7")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());

    verify(users).delete(user);
  }

  @Test
  void missingApplicationAccountReturnsNotFound() throws Exception {
    when(users.findByGoogleSubject(SUBJECT)).thenReturn(Optional.empty());
    mvc.perform(delete("/api/v1/users/me")
        .with(jwt().jwt(token -> token.subject(SUBJECT))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isNotFound());
    verify(users, never()).delete(any());
  }

  @ParameterizedTest
  @EnumSource(UserRole.class)
  void onlyAdministratorsCanListUsers(UserRole role) throws Exception {
    if (role == UserRole.ADMIN) {
      when(users.findAll(any(Pageable.class))).thenReturn(Page.empty());
    }
    mvc.perform(get("/api/v1/users").with(jwt()
            .authorities(new SimpleGrantedAuthority("ROLE_" + role.name()))))
        .andExpect(status().is(role == UserRole.ADMIN ? 200 : 403));
    if (role != UserRole.ADMIN) verifyNoInteractions(users);
  }
}
