package cst438.project2.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.InviteId;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.InviteRepository;
import cst438.project2.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.time.OffsetDateTime;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// No test transaction: HTTP writes must commit before JDBC and later requests can see them.
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
  @Autowired private RequestMappingHandlerMapping mappings;
  @MockitoBean(name = "googleJwtDecoder") private JwtDecoder decoder;

  private User admin;
  private User recipient;
  private Event firstEvent;
  private Event secondEvent;

  @BeforeEach
  void setUp() {
    admin = saveUser(UserRole.ADMIN);
    recipient = saveUser(UserRole.USER);
    firstEvent = events.save(event("First event"));
    secondEvent = events.save(event("Second event"));
    bearerTokenFor("admin-token", admin);
    bearerTokenFor("user-token", recipient);
  }

  @Test
  void createdInvitesCommitAndPersonalRoutesReturnOnlyTheCallersInvitesWithPagination()
      throws Exception {
    createInvite(firstEvent, "First");
    createInvite(secondEvent, "Second");
    assertEquals("First", rows().get(0).get("message"));
    assertEquals(firstEvent.getId(), invites.findById(
        new InviteId(recipient.getUserId(), firstEvent.getId())).orElseThrow().getEvent().getId());

    mvc.perform(get("/api/v1/invites/me?size=20&sort=inviteId.eventId,asc")
            .queryParam("userId", admin.getUserId().toString())
            .header("Authorization", "Bearer user-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.totalElements").value(2))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].eventId").value(firstEvent.getId()))
        .andExpect(jsonPath("$.content[1].eventId").value(secondEvent.getId()));
    mvc.perform(get("/api/v1/invites/me?page=1&size=1&sort=inviteId.eventId,asc")
            .header("Authorization", "Bearer user-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.totalElements").value(2))
        .andExpect(jsonPath("$.page.totalPages").value(2))
        .andExpect(jsonPath("$.content[0].eventId").value(secondEvent.getId()));
    mvc.perform(get("/api/v1/invites/me/" + firstEvent.getId())
            .header("Authorization", "Bearer user-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(recipient.getUserId()));
  }

  @Test
  void duplicatePostReturns409AndPreservesTheAcceptedInvitation() throws Exception {
    createInvite(firstEvent, "Original");
    acceptFirstInvite();
    var original = rows();
    mvc.perform(inviteRequest("POST", firstEvent, recipient, "Replacement")
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isConflict());
    assertEquals(original, rows());
    mvc.perform(patch("/api/v1/invites/users/" + recipient.getUserId() + "/events/" + firstEvent.getId())
            .header("Authorization", "Bearer admin-token").contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"REVOKED\",\"message\":\"Cancelled\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REVOKED"))
        .andExpect(jsonPath("$.message").value("Cancelled"));
    assertEquals("REVOKED", rows().get(0).get("status"));
  }

  @ParameterizedTest(name = "{0} requires current ownership of this event")
  @ValueSource(strings = {"GET", "POST", "PUT", "PATCH"})
  void managerAccessIsScopedToBothIdsAndRevocationAppliesToTheSameToken(String method)
      throws Exception {
    createInvite(firstEvent, "Original");
    User manager = saveUser(UserRole.MANAGER);
    User otherManager = saveUser(UserRole.MANAGER);
    assignManager(secondEvent, manager);
    assignManager(firstEvent, otherManager);
    bearerTokenFor("manager-token", manager);
    // POST needs a new recipient; the other operations address the existing invite.
    User target = method.equals("POST") ? admin : recipient;
    var original = rows();
    mvc.perform(inviteRequest(method, firstEvent, target, "Denied")
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
    assertEquals(original, rows());

    assignManager(firstEvent, manager);
    mvc.perform(inviteRequest(method, firstEvent, target, "Allowed")
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().is(method.equals("POST") ? 201 : method.equals("DELETE") ? 204 : 200));
    if (method.equals("PUT") || method.equals("PATCH")) {
      assertEquals("Allowed", rows().get(0).get("message"));
      assertEquals(method.equals("PUT") ? "ACCEPTED" : "PENDING", rows().get(0).get("status"));
    }

    mvc.perform(delete(managerPath(firstEvent, manager)).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());
    var afterAllowedRequest = rows();
    mvc.perform(inviteRequest(method, firstEvent, target, "Denied after revocation")
            .header("Authorization", "Bearer manager-token"))
        .andExpect(status().isForbidden());
    assertEquals(afterAllowedRequest, rows());
    // Removing this manager must leave the other manager's access intact.
    bearerTokenFor("other-manager-token", otherManager);
    mvc.perform(inviteRequest("GET", firstEvent, recipient, "")
            .header("Authorization", "Bearer other-manager-token"))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @EnumSource(value = UserRole.class, names = {"USER", "MANAGER"})
  void nonAdminsCannotUseAdminListsOrGrantOwnership(UserRole role) throws Exception {
    createInvite(firstEvent, "Keep");
    User caller = role == UserRole.USER ? recipient : saveUser(role);
    bearerTokenFor("caller-token", caller);
    for (String path : List.of("/api/v1/invites", "/api/v1/invites/users/" + recipient.getUserId(),
        "/api/v1/invites/users/" + recipient.getUserId() + "/events/" + firstEvent.getId())) {
      mvc.perform(get(path).header("Authorization", "Bearer caller-token"))
          .andExpect(status().isForbidden());
    }
    mvc.perform(put(managerPath(firstEvent, caller)).header("Authorization", "Bearer caller-token"))
        .andExpect(status().isForbidden());
    mvc.perform(delete(managerPath(firstEvent, caller)).header("Authorization", "Bearer caller-token"))
        .andExpect(status().isForbidden());
    mvc.perform(inviteRequest("DELETE", firstEvent, recipient, "")
            .header("Authorization", "Bearer caller-token"))
        .andExpect(status().isForbidden());
    if (role == UserRole.USER) {
      mvc.perform(put(managerPath(firstEvent, caller))).andExpect(status().isUnauthorized());
      var original = rows();
      for (String method : List.of("GET", "POST", "PUT", "PATCH", "DELETE")) {
        mvc.perform(inviteRequest(method, firstEvent, recipient, "Denied")
                .header("Authorization", "Bearer caller-token"))
            .andExpect(status().isForbidden());
      }
      assertEquals(original, rows());
    }
  }

  @Test
  void administratorCanReadAndDeleteWithoutOwnershipAndOnlyTheChosenPairIsRemoved()
      throws Exception {
    createInvite(firstEvent, "Target");
    createInvite(secondEvent, "Keep other event");
    jdbc.update("insert into invites (user_id, event_id, status) values (?, ?, 'PENDING')",
        admin.getUserId(), firstEvent.getId());
    acceptFirstInvite();
    mvc.perform(get("/api/v1/events")).andExpect(status().isOk())
        .andExpect(jsonPath("$.page.totalElements").isNumber());
    mvc.perform(get("/api/v1/events/" + firstEvent.getId())).andExpect(status().isOk())
        .andExpect(jsonPath("$.updatedAt").isString());
    mvc.perform(inviteRequest("GET", firstEvent, recipient, "")
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(2));
    mvc.perform(get("/api/v1/events/" + firstEvent.getId() + "/totalAttendees"))
        .andExpect(status().isOk()).andExpect(jsonPath("$").value(1));
    mvc.perform(get("/api/v1/events/" + firstEvent.getId() + "/totalAttendees?archived=true"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    jdbc.update("insert into archived_events (arc_event_id, arc_event_size) values (?, ?)",
        firstEvent.getId(), 42);
    mvc.perform(get("/api/v1/events/" + firstEvent.getId() + "/totalAttendees?archived=true"))
        .andExpect(status().isOk()).andExpect(jsonPath("$").value(42));
    mvc.perform(inviteRequest("DELETE", firstEvent, recipient, "")
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent()).andExpect(content().string(""));
    assertEquals(1, rows().size());
    assertEquals(admin.getUserId().longValue(), ((Number) rows().get(0).get("user_id")).longValue());
    assertEquals(1L, jdbc.queryForObject(
        "select count(*) from invites where user_id = ? and event_id = ?", Long.class,
        recipient.getUserId(), secondEvent.getId()));
    mvc.perform(get("/api/v1/events/" + firstEvent.getId() + "/totalAttendees"))
        .andExpect(status().isOk()).andExpect(jsonPath("$").value(0));
    mvc.perform(inviteRequest("DELETE", firstEvent, recipient, "")
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/invites/me/" + firstEvent.getId())
            .header("Authorization", "Bearer user-token"))
        .andExpect(status().isNotFound());
  }

  private User saveUser(UserRole role) {
    String suffix = UUID.randomUUID().toString();
    User user = new User(null, role.name() + "-" + suffix, "subject-" + suffix, role, null, null);
    user.setEmail(suffix + "@example.com");
    return users.save(user);
  }

  @Test
  @SuppressWarnings("unchecked")
  void everyDocumentedOpenApiRouteHasAControllerMapping() {
    YamlMapFactoryBean yaml = new YamlMapFactoryBean();
    yaml.setResources(new FileSystemResource("docs/openapi.yaml"));
    Map<String, Object> document = yaml.getObject();
    Set<String> actual = new HashSet<>();
    mappings.getHandlerMethods().keySet().forEach(mapping -> mapping.getPatternValues().forEach(path ->
        mapping.getMethodsCondition().getMethods().forEach(method -> actual.add(method.name().toLowerCase() + " " + path))));
    Map<String, Object> paths = (Map<String, Object>) document.get("paths");
    for (var entry : paths.entrySet()) {
      for (Object method : ((Map<?, ?>) entry.getValue()).keySet()) {
        if (Set.of("get", "post", "put", "patch", "delete").contains(method)) {
          assertTrue(actual.contains(method + " " + entry.getKey()),
              "Missing controller mapping for " + method + " " + entry.getKey());
        }
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"ACCEPTED", "DECLINED"})
  void documentedReplyRouteCommitsTheResponseAndRejectsASecondReply(String response) throws Exception {
    createInvite(firstEvent, "Please reply");
    var result = mvc.perform(patch("/api/v1/invites/me/" + firstEvent.getId())
            .header("Authorization", "Bearer user-token").contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"" + response + "\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(response))
        .andExpect(jsonPath("$.message").value("Please reply"))
        .andExpect(jsonPath("$.respondedAt").isString()).andReturn();
    var json = tools.jackson.databind.json.JsonMapper.builder().build()
        .readTree(result.getResponse().getContentAsString());
    OffsetDateTime.parse(json.get("respondedAt").asString());
    assertEquals(response, rows().get(0).get("status"));
    mvc.perform(patch("/api/v1/invites/me/" + firstEvent.getId())
            .header("Authorization", "Bearer user-token").contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"" + response + "\"}"))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(409));
  }

  private Event event(String name) {
    return new Event(null, name, "Description", LocalDateTime.of(2026, 11, 1, 12, 0),
        admin.getUserId(), null, 1L);
  }

  private void createInvite(Event event, String message) throws Exception {
    mvc.perform(inviteRequest("POST", event, recipient, message)
            .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(header().string("Location", "/api/v1/invites/users/" + recipient.getUserId()
            + "/events/" + event.getId()));
  }

  private MockHttpServletRequestBuilder inviteRequest(String method, Event event, User target, String message) {
    var request = switch (method) {
      case "GET" -> get("/api/v1/invites/events/" + event.getId());
      case "POST" -> post("/api/v1/invites");
      case "PUT" -> put("/api/v1/invites");
      case "PATCH" -> patch("/api/v1/invites");
      case "DELETE" -> delete("/api/v1/invites/users/" + target.getUserId() + "/events/" + event.getId());
      default -> throw new IllegalArgumentException(method);
    };
    if (method.equals("GET") || method.equals("DELETE")) return request;
    String status = method.equals("PUT") ? ",\"status\":\"ACCEPTED\"" : "";
    return request.contentType(MediaType.APPLICATION_JSON).content("""
        {"userId":%d,"eventId":%d,"message":"%s"%s}
        """.formatted(target.getUserId(), event.getId(), message, status));
  }

  private String managerPath(Event event, User manager) {
    return "/api/v1/events/" + event.getId() + "/managers/" + manager.getUserId();
  }

  private void assignManager(Event event, User manager) throws Exception {
    mvc.perform(put(managerPath(event, manager)).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());
  }

  private void bearerTokenFor(String value, User user) {
    // The role claim must not override the role in the database.
    when(decoder.decode(value)).thenReturn(Jwt.withTokenValue(value).header("alg", "RS256")
        .subject(user.getGoogleSubject()).claim("role", "ADMIN").build());
  }

  private void acceptFirstInvite() {
    jdbc.update("update invites set status = 'ACCEPTED', responded_at = now() where user_id = ? and event_id = ?",
        recipient.getUserId(), firstEvent.getId());
  }

  private List<Map<String, Object>> rows() {
    return jdbc.queryForList("""
        select user_id, event_id, status::text as status, message from invites
        where event_id = ? order by user_id
        """, firstEvent.getId());
  }
}
