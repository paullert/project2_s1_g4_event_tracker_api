package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.model.*;
import cst438.project2.backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;

@SpringJUnitConfig(ManagerOwnershipSecurityTest.Config.class)
class ManagerOwnershipSecurityTest {
  private static final Long EVENT_ID = 20L;
  private static final Long MANAGER_ID = 7L;
  private static final LocalDateTime START = LocalDateTime.of(2026, 11, 1, 12, 0);

  @Configuration
  @EnableMethodSecurity
  @Import({UserService.class, EventOwnershipService.class, EventService.class})
  static class Config {
    @Bean UserRepository users() { return mock(UserRepository.class); }
    @Bean EventRepository events() { return mock(EventRepository.class); }
    @Bean InviteRepository invites() { return mock(InviteRepository.class); }
    @Bean ManagerEventRepository managerEvents() { return mock(ManagerEventRepository.class); }
  }

  @Autowired private UserRepository users;
  @Autowired private EventRepository events;
  @Autowired private InviteRepository invites;
  @Autowired private ManagerEventRepository managerEvents;
  @Autowired private EventService eventService;
  @Autowired private EventOwnershipService ownership;
  private Event event;

  @BeforeEach
  void setUp() {
    reset(users, events, invites, managerEvents);
    event = new Event(EVENT_ID, "Event", "Description", START, 99L, null, 1L);
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  private JwtAuthenticationToken signIn(UserRole role, Long userId) {
    User user = new User(userId, "user-" + userId, "subject-" + userId, role, null, null);
    Jwt jwt = Jwt.withTokenValue("test").header("alg", "RS256")
        .subject(user.getGoogleSubject()).build();
    JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt,
        List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(users.findByGoogleSubject(user.getGoogleSubject())).thenReturn(Optional.of(user));
    return auth;
  }

  @Test
  void twoAssignedManagersCanEditTheSameEvent() {
    when(events.findById(EVENT_ID)).thenReturn(Optional.of(event));
    when(events.save(event)).thenReturn(event);
    for (long managerId : new long[]{MANAGER_ID, 8L}) {
      var auth = signIn(UserRole.MANAGER, managerId);
      when(managerEvents.existsById(new ManagerEventId(EVENT_ID, managerId))).thenReturn(true);
      eventService.patchEvent(auth, EVENT_ID,
          new EventRequest("Updated", null, null, null, null));
    }
    assertEquals("Updated", event.getEventName());
    verify(events, times(2)).save(event);
  }

  @Test
  void unassignedManagerCannotEditOrDeleteAnotherEvent() {
    var auth = signIn(UserRole.MANAGER, MANAGER_ID);
    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> eventService.patchEvent(auth, EVENT_ID,
            new EventRequest("Changed", null, null, null, null)));
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    assertThrows(ResponseStatusException.class, () -> eventService.deleteEvent(auth, EVENT_ID));
    assertEquals("Event", event.getEventName());
    verifyNoInteractions(events, invites);
  }

  @Test
  void administratorCanEditWithoutAnAssignment() {
    var auth = signIn(UserRole.ADMIN, 1L);
    when(events.findById(EVENT_ID)).thenReturn(Optional.of(event));
    when(events.save(event)).thenReturn(event);
    eventService.putEvent(auth, EVENT_ID,
        new EventRequest("Admin edit", "Description", START, null, 1L));
    assertEquals("Admin edit", event.getEventName());
    verifyNoInteractions(managerEvents);
  }

  @Test
  void ordinaryUserCannotEditEvenIfAnAssignmentExists() {
    var auth = signIn(UserRole.USER, MANAGER_ID);
    assertThrows(AccessDeniedException.class, () -> eventService.patchEvent(auth, EVENT_ID,
        new EventRequest("Changed", null, null, null, null)));
    verifyNoInteractions(events, managerEvents);
  }

  @Test
  void managerCreatingAnEventIsAssignedAndCannotSpoofCreator() {
    var auth = signIn(UserRole.MANAGER, MANAGER_ID);
    when(events.save(any(Event.class))).thenAnswer(call -> {
      Event created = call.getArgument(0);
      created.setId(EVENT_ID);
      return created;
    });
    var response = eventService.createEvent(auth,
        new EventRequest("New", "Description", START, 999L, 1L));
    assertEquals(MANAGER_ID, response.creatorId());
    verify(managerEvents).save(argThat(assignment ->
        assignment.getId().equals(new ManagerEventId(EVENT_ID, MANAGER_ID))));
  }

  @Test
  void deletingOwnedEventRemovesDependentRowsFirst() {
    var auth = signIn(UserRole.MANAGER, MANAGER_ID);
    when(managerEvents.existsById(new ManagerEventId(EVENT_ID, MANAGER_ID))).thenReturn(true);
    when(events.findById(EVENT_ID)).thenReturn(Optional.of(event));
    eventService.deleteEvent(auth, EVENT_ID);
    var order = inOrder(invites, managerEvents, events);
    order.verify(invites).deleteForEvent(EVENT_ID);
    order.verify(managerEvents).deleteForEvent(EVENT_ID);
    order.verify(events).delete(event);
  }

  @Test
  void managerCannotGrantOrRevokeOwnershipEvenForTheirOwnEvent() {
    signIn(UserRole.MANAGER, MANAGER_ID);
    assertThrows(AccessDeniedException.class, () -> ownership.assignManager(EVENT_ID, 8L));
    assertThrows(AccessDeniedException.class, () -> ownership.removeManager(EVENT_ID, 8L));
    verifyNoInteractions(managerEvents, events);
  }

  @Test
  void assigningAnOrdinaryUserIsRejectedAndExistingAssignmentIsIdempotent() {
    signIn(UserRole.ADMIN, 1L);
    when(events.existsById(EVENT_ID)).thenReturn(true);
    User target = new User(MANAGER_ID, "target", "target-subject", UserRole.USER, null, null);
    when(users.findById(MANAGER_ID)).thenReturn(Optional.of(target));
    var ex = assertThrows(ResponseStatusException.class,
        () -> ownership.assignManager(EVENT_ID, MANAGER_ID));
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    target.setRole(UserRole.MANAGER);
    when(managerEvents.existsById(new ManagerEventId(EVENT_ID, MANAGER_ID))).thenReturn(true);
    ownership.assignManager(EVENT_ID, MANAGER_ID);
    verify(managerEvents, never()).save(any());
  }
}
