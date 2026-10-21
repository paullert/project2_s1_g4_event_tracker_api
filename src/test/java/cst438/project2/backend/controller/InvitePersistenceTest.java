package cst438.project2.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.InviteId;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.InviteRepository;
import cst438.project2.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// No test transaction: each HTTP write must commit before the JDBC/GET assertions.
@SpringBootTest(properties = "google.oauth2.client-id=test-client")
@AutoConfigureMockMvc
@Testcontainers
class InvitePersistenceTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
      .withInitScript("database.sql");

  @Autowired private MockMvc mvc;
  @Autowired private UserRepository users;
  @Autowired private EventRepository events;
  @Autowired private InviteRepository invites;
  @Autowired private JdbcTemplate jdbc;
  @MockitoBean(name = "googleJwtDecoder") private JwtDecoder decoder;

  private User admin;
  private User recipient;
  private Event firstEvent;
  private Event secondEvent;

  @BeforeEach
  void setUp() {
    String suffix = UUID.randomUUID().toString();
    admin = new User(null, "admin-" + suffix, "admin-sub-" + suffix,
        UserRole.ADMIN, null, null);
    admin.setEmail("admin-" + suffix + "@example.com");
    admin = users.save(admin);
    recipient = new User(null, "recipient-" + suffix, "recipient-sub-" + suffix,
        UserRole.USER, null, null);
    recipient.setEmail("recipient-" + suffix + "@example.com");
    recipient = users.save(recipient);
    firstEvent = events.save(event("First event"));
    secondEvent = events.save(event("Second event"));
  }

  @Test
  void successfulPostCommitsTheRowAndMakesItReadableInAnotherRequest() throws Exception {
    mvc.perform(createInvite(firstEvent, "Welcome"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value(recipient.getUserId()))
        .andExpect(jsonPath("$.eventId").value(firstEvent.getId()));

    assertEquals("Welcome", jdbc.queryForObject(
        "select message from invites where user_id = ? and event_id = ?", String.class,
        recipient.getUserId(), firstEvent.getId()));
    assertEquals(firstEvent.getId(), invites.findById(
        new InviteId(recipient.getUserId(), firstEvent.getId())).orElseThrow()
        .getEvent().getId());

    mvc.perform(get("/api/v1/invites/me").with(jwt()
            .jwt(token -> token.subject(recipient.getGoogleSubject()))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].eventId").value(firstEvent.getId()));
  }

  @Test
  void myInvitesReturnsBothEventsAndHonorsPagination() throws Exception {
    mvc.perform(createInvite(firstEvent, "First")).andExpect(status().isCreated());
    mvc.perform(createInvite(secondEvent, "Second")).andExpect(status().isCreated());

    mvc.perform(get("/api/v1/invites/me?size=20&sort=inviteId.eventId,asc")
            .queryParam("userId", admin.getUserId().toString())
            .with(jwt().jwt(token -> token.subject(recipient.getGoogleSubject()))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].eventId").value(firstEvent.getId()))
        .andExpect(jsonPath("$.content[1].eventId").value(secondEvent.getId()));

    mvc.perform(get("/api/v1/invites/me?page=1&size=1&sort=inviteId.eventId,asc")
            .with(jwt().jwt(token -> token.subject(recipient.getGoogleSubject()))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].eventId").value(secondEvent.getId()));
  }

  @Test
  void repeatedPostReturnsConflictAndPreservesTheOriginalInvitation() throws Exception {
    mvc.perform(createInvite(firstEvent, "Original")).andExpect(status().isCreated());
    jdbc.update("""
        update invites set status = 'ACCEPTED', responded_at = now()
        where user_id = ? and event_id = ?
        """, recipient.getUserId(), firstEvent.getId());
    mvc.perform(createInvite(firstEvent, "Replacement")).andExpect(status().isConflict());

    assertEquals(1L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), firstEvent.getId()));
    assertEquals("Original", jdbc.queryForObject(
        "select message from invites where user_id = ? and event_id = ?", String.class,
        recipient.getUserId(), firstEvent.getId()));
    assertEquals("ACCEPTED", jdbc.queryForObject(
        "select status::text from invites where user_id = ? and event_id = ?", String.class,
        recipient.getUserId(), firstEvent.getId()));
  }

  @Test
  void managerAssignedToAnotherEventCannotReadThisEventsInvitations() throws Exception {
    mvc.perform(createInvite(firstEvent, "First")).andExpect(status().isCreated());
    mvc.perform(createInvite(secondEvent, "Second")).andExpect(status().isCreated());
    User manager = manager("manager");
    assignManager(secondEvent, manager);
    bearerTokenFor("manager-token", manager);

    mvc.perform(get("/api/v1/invites/events/" + firstEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/invites/events/" + secondEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].eventId").value(secondEvent.getId()));
  }

  @Test
  void assignmentForAnotherManagerDoesNotGrantAccessAndRevocationTakesEffectImmediately()
      throws Exception {
    mvc.perform(createInvite(firstEvent, "First")).andExpect(status().isCreated());
    User manager = manager("manager");
    User otherManager = manager("other-manager");
    assignManager(firstEvent, otherManager);
    bearerTokenFor("manager-token", manager);

    mvc.perform(get("/api/v1/invites/events/" + firstEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());

    assignManager(firstEvent, manager);
    mvc.perform(get("/api/v1/invites/events/" + firstEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));

    mvc.perform(delete("/api/v1/events/" + firstEvent.getId() + "/managers/" + manager.getUserId())
            .with(jwt().jwt(token -> token.subject(admin.getGoogleSubject()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/invites/events/" + firstEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void managerCannotUseAdminListingRoutesToBypassEventOwnership() throws Exception {
    User manager = manager("manager");
    bearerTokenFor("manager-token", manager);

    mvc.perform(get("/api/v1/invites").header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/invites/users/" + recipient.getUserId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void administratorsCanReadEventInvitationsWithoutAnAssignment() throws Exception {
    mvc.perform(createInvite(firstEvent, "First")).andExpect(status().isCreated());
    bearerTokenFor("admin-token", admin);

    mvc.perform(get("/api/v1/invites/events/" + firstEvent.getId())
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void unassignedManagerCannotCreateInvitesEvenWhenAssignedToAnotherEvent() throws Exception {
    User manager = manager("manager");
    assignManager(secondEvent, manager);
    bearerTokenFor("manager-token", manager);

    mvc.perform(post("/api/v1/invites").header("Authorization", "Bearer manager-token")
            .contentType(MediaType.APPLICATION_JSON).content("""
                {"userId":%d,"eventId":%d,"status":"PENDING","message":"Unauthorized"}
                """.formatted(recipient.getUserId(), firstEvent.getId())))
        .andExpect(status().isForbidden());

    assertEquals(0L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), firstEvent.getId()));
  }

  @Test
  void creationRequiresAssignmentAndRevocationDeniesEvenAnExistingInvite() throws Exception {
    User manager = manager("manager");
    bearerTokenFor("manager-token", manager);
    assignManager(firstEvent, manager);

    mvc.perform(post("/api/v1/invites").header("Authorization", "Bearer manager-token")
            .contentType(MediaType.APPLICATION_JSON).content("""
                {"userId":%d,"eventId":%d,"message":"Original"}
                """.formatted(recipient.getUserId(), firstEvent.getId())))
        .andExpect(status().isCreated());

    mvc.perform(delete("/api/v1/events/" + firstEvent.getId() + "/managers/" + manager.getUserId())
            .with(jwt().jwt(token -> token.subject(admin.getGoogleSubject()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());

    mvc.perform(post("/api/v1/invites").header("Authorization", "Bearer manager-token")
            .contentType(MediaType.APPLICATION_JSON).content("""
                {"userId":%d,"eventId":%d,"message":"Unauthorized replacement"}
                """.formatted(recipient.getUserId(), firstEvent.getId())))
        .andExpect(status().isForbidden());

    assertEquals("Original", jdbc.queryForObject(
        "select message from invites where user_id = ? and event_id = ?", String.class,
        recipient.getUserId(), firstEvent.getId()));
  }

  @Test
  void ordinaryUserCannotCreateInvites() throws Exception {
    bearerTokenFor("user-token", recipient);

    mvc.perform(post("/api/v1/invites").header("Authorization", "Bearer user-token")
            .contentType(MediaType.APPLICATION_JSON).content("""
                {"userId":%d,"eventId":%d,"message":"Unauthorized"}
                """.formatted(recipient.getUserId(), firstEvent.getId())))
        .andExpect(status().isForbidden());

    assertEquals(0L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), firstEvent.getId()));
  }

  @ParameterizedTest
  @EnumSource(value = UserRole.class, names = {"ADMIN", "MANAGER"})
  void permittedCallerDeletesOnlyTheRequestedInviteAndUpdatesAttendeeCount(UserRole role)
      throws Exception {
    mvc.perform(createInvite(firstEvent, "Target")).andExpect(status().isCreated());
    mvc.perform(createInvite(secondEvent, "Keep other event")).andExpect(status().isCreated());
    jdbc.update("""
        insert into invites (user_id, event_id, status) values (?, ?, 'PENDING')
        """, admin.getUserId(), firstEvent.getId());
    jdbc.update("""
        update invites set status = 'ACCEPTED', responded_at = now()
        where user_id = ? and event_id = ?
        """, recipient.getUserId(), firstEvent.getId());
    User caller = role == UserRole.ADMIN ? admin : manager("manager");
    if (role == UserRole.MANAGER) assignManager(firstEvent, caller);
    bearerTokenFor("delete-token", caller);

    mvc.perform(get("/api/v1/events/" + firstEvent.getId() + "/totalAttendees"))
        .andExpect(status().isOk()).andExpect(jsonPath("$").value(1));
    mvc.perform(delete("/api/v1/invites/users/" + recipient.getUserId()
                + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer delete-token"))
        .andExpect(status().isNoContent());

    assertEquals(0L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), firstEvent.getId()));
    assertEquals(1L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), secondEvent.getId()));
    assertEquals(1L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        admin.getUserId(), firstEvent.getId()));
    mvc.perform(get("/api/v1/events/" + firstEvent.getId() + "/totalAttendees"))
        .andExpect(status().isOk()).andExpect(jsonPath("$").value(0));
    mvc.perform(delete("/api/v1/invites/users/" + recipient.getUserId()
                + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer delete-token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void unassignedManagerCannotDeleteAnInviteOrProbeWhetherItExists() throws Exception {
    mvc.perform(createInvite(firstEvent, "Keep")).andExpect(status().isCreated());
    User manager = manager("manager");
    assignManager(secondEvent, manager);
    bearerTokenFor("manager-token", manager);

    mvc.perform(delete("/api/v1/invites/users/" + recipient.getUserId()
                + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/v1/invites/users/" + admin.getUserId()
                + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());

    assertEquals(1L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), firstEvent.getId()));
  }

  @Test
  void ordinaryUserCannotDeleteEvenTheirOwnInvitation() throws Exception {
    mvc.perform(createInvite(firstEvent, "Keep")).andExpect(status().isCreated());
    bearerTokenFor("user-token", recipient);

    mvc.perform(delete("/api/v1/invites/users/" + recipient.getUserId()
                + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer user-token"))
        .andExpect(status().isForbidden());

    assertEquals(1L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), firstEvent.getId()));
  }

  @Test
  void administratorDeletingAMissingInvitationGets404() throws Exception {
    bearerTokenFor("admin-token", admin);
    mvc.perform(delete("/api/v1/invites/users/" + recipient.getUserId()
                + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNotFound());
  }

  private User manager(String name) {
    String suffix = UUID.randomUUID().toString();
    User manager = new User(null, name + "-" + suffix, "manager-sub-" + suffix,
        UserRole.MANAGER, null, null);
    manager.setEmail(name + "-" + suffix + "@example.com");
    return users.save(manager);
  }

  private void assignManager(Event event, User manager) throws Exception {
    mvc.perform(put("/api/v1/events/" + event.getId() + "/managers/" + manager.getUserId())
            .with(jwt().jwt(token -> token.subject(admin.getGoogleSubject()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());
  }

  private void bearerTokenFor(String tokenValue, User user) {
    // Even an ADMIN claim cannot override this manager's role stored in the database.
    when(decoder.decode(tokenValue)).thenReturn(Jwt.withTokenValue(tokenValue)
        .header("alg", "RS256").subject(user.getGoogleSubject()).claim("role", "ADMIN").build());
  }

  private MockHttpServletRequestBuilder createInvite(Event event, String message) {
    return post("/api/v1/invites").with(jwt()
            .jwt(token -> token.subject(admin.getGoogleSubject()))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"userId":%d,"eventId":%d,"message":"%s"}
            """.formatted(recipient.getUserId(), event.getId(), message));
  }

  private Event event(String name) {
    return new Event(null, name, "Description", LocalDateTime.of(2026, 11, 1, 12, 0),
        admin.getUserId(), null, 1L);
  }
}
