package cst438.project2.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(OauthController.class)
@Import(SecurityConfig.class)
class OauthControllerTest {
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
  void meReturnsApplicationAccountForAuthenticatedSubject() throws Exception {
    when(users.findUserFromAuthentication(any())).thenReturn(user);
    mvc.perform(get("/auth/me").with(jwt().jwt(token -> token.subject("google-subject"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(7))
        .andExpect(jsonPath("$.role").value("USER"));
    verify(users).findUserFromAuthentication(argThat(auth -> "google-subject".equals(auth.getName())));
  }

  @Test
  void registrationAcceptsValidGoogleIdentityWithoutAnApplicationRole() throws Exception {
    when(users.registerUser(any(), eq("alex"))).thenReturn(user);
    mvc.perform(post("/auth/register")
        .with(jwt().jwt(token -> token.subject("google-subject")).authorities(List.of()))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"alex\",\"email\":\"alex@example.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(7));
    verify(users).registerUser(argThat(token -> "google-subject".equals(token.getSubject())), eq("alex"));
  }

  @ParameterizedTest
  @CsvSource({"GET,/auth/me", "POST,/auth/register"})
  void authenticationIsRequired(String method, String path) throws Exception {
    mvc.perform(request(HttpMethod.valueOf(method), path)
        .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(users);
  }

  @Test
  void invalidBearerTokenReturns401() throws Exception {
    when(decoder.decode("invalid-token")).thenThrow(new BadJwtException("Invalid token"));
    mvc.perform(get("/auth/me").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(users, converter);
  }

  @Test
  void unregisteredAccountReturns404() throws Exception {
    when(users.findUserFromAuthentication(any())).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    mvc.perform(get("/auth/me").with(jwt())).andExpect(status().isNotFound());
  }

  @Test
  void duplicateUsernameReturns409() throws Exception {
    when(users.registerUser(any(), eq("alex"))).thenThrow(
        new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));
    mvc.perform(post("/auth/register").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"alex\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void registrationRequiresARequestBody() throws Exception {
    mvc.perform(post("/auth/register").with(jwt()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(users);
  }

  @Test
  void malformedRegistrationJsonReturns400() throws Exception {
    mvc.perform(post("/auth/register").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{broken"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(users);
  }
}
