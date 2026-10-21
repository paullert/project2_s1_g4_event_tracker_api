package cst438.project2.backend.service;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cst438.project2.backend.controller.UserController;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class AuthTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtAuthConverter jwtAuthConverter;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @Test
  void usersRouteWithoutTokenReturns401() throws Exception {
    mockMvc.perform(get("/api/v1/users"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void usersRouteForRegularUserReturns403() throws Exception {
    mockMvc.perform(
        get("/api/v1/users")
            .with(jwt().authorities(
                new SimpleGrantedAuthority("ROLE_USER")
            ))
    ).andExpect(status().isForbidden());
  }

  @Test
  void usersRouteForAdminReturns200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    when(userService.getUsers(pageable))
        .thenReturn(Page.empty());

    mockMvc.perform(
        get("/api/v1/users")
            .with(jwt().authorities(
                new SimpleGrantedAuthority("ROLE_ADMIN")
            ))
    ).andExpect(status().isOk());
  }
}
