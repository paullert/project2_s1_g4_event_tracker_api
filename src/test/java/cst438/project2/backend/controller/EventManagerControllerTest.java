package cst438.project2.backend.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.ManagerEventRepository;
import cst438.project2.backend.repository.UserRepository;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.EventOwnershipService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventManagerController.class)
@Import({SecurityConfig.class, EventOwnershipService.class})
class EventManagerControllerTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private EventRepository events;
  @MockitoBean private UserRepository users;
  @MockitoBean private ManagerEventRepository managerEvents;
  @MockitoBean private JwtAuthConverter converter;
  @MockitoBean private JwtDecoder decoder;

  @Test
  void assignmentRequiresAuthentication() throws Exception {
    mvc.perform(put("/api/v1/events/20/managers/7"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(managerEvents);
  }

  @Test
  void managerCannotAssignAnotherManager() throws Exception {
    mvc.perform(put("/api/v1/events/20/managers/7")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
        .andExpect(status().isForbidden());
    verifyNoInteractions(managerEvents);
  }

  @Test
  void adminCanAssignManager() throws Exception {
    when(events.existsById(20L)).thenReturn(true);
    when(users.findById(7L)).thenReturn(Optional.of(
        new User(7L, "manager", "subject", UserRole.MANAGER, null, null)));
    mvc.perform(put("/api/v1/events/20/managers/7")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());
    verify(managerEvents).save(any());
  }

  @Test
  void adminCanRemoveManager() throws Exception {
    when(events.existsById(20L)).thenReturn(true);
    mvc.perform(delete("/api/v1/events/20/managers/7")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());
    verify(managerEvents).deleteById(any());
  }
}
