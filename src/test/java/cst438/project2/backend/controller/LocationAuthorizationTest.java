package cst438.project2.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.LocationRepository;
import cst438.project2.backend.repository.UserRepository;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.LocationService;
import cst438.project2.backend.service.UserService;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Keep the converter and services real so these requests exercise method security.
@WebMvcTest(LocationController.class)
@Import({SecurityConfig.class, JwtAuthConverter.class, UserService.class, LocationService.class})
class LocationAuthorizationTest {
  private static final String SUBJECT = "registered-google-subject";
  private static final String BODY = """
      {"locationName":"Library","address":"100 Main St","locationDescription":"Meeting room"}
      """;

  @Autowired private MockMvc mvc;
  @MockitoBean private UserRepository users;
  @MockitoBean private LocationRepository locations;
  @MockitoBean private JwtDecoder decoder;

  static Stream<Arguments> rolesAndWriteMethods() {
    return Arrays.stream(UserRole.values()).flatMap(role ->
        Stream.of("POST", "PUT", "PATCH", "DELETE").map(method -> Arguments.of(role, method)));
  }

  @ParameterizedTest(name = "{0} sending {1}")
  @MethodSource("rolesAndWriteMethods")
  void onlyDatabaseAdministratorsCanChangeLocations(UserRole role, String method) throws Exception {
    User caller = authenticateAs(role);
    Location existing = new Location(7L, "Old name", "Old address", "Old description", caller, null, null);
    when(locations.findById(7L)).thenReturn(Optional.of(existing));
    when(locations.save(any(Location.class))).thenAnswer(invocation -> {
      Location saved = invocation.getArgument(0);
      saved.setLocationId(7L);
      return saved;
    });
    boolean admin = role == UserRole.ADMIN;
    int expectedStatus = admin ? (method.equals("POST") ? 201 : method.equals("DELETE") ? 204 : 200) : 403;
    String path = method.equals("POST") ? "/api/v1/locations" : "/api/v1/locations/7";

    mvc.perform(request(HttpMethod.valueOf(method), path)
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().is(expectedStatus));

    if (!admin) {
      verify(locations, never()).save(any());
      verify(locations, never()).delete(any());
    } else if (method.equals("DELETE")) {
      verify(locations).delete(existing);
    } else {
      verify(locations).save(any(Location.class));
    }
  }

  @Test
  void roleChangeTakesEffectWhenTheSameTokenIsReused() throws Exception {
    User caller = authenticateAs(UserRole.ADMIN);
    when(locations.save(any(Location.class))).thenAnswer(invocation -> {
      Location saved = invocation.getArgument(0);
      assertSame(caller, saved.getCreator());
      saved.setLocationId(7L);
      return saved;
    });

    mvc.perform(post("/api/v1/locations").header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isCreated());

    caller.setRole(UserRole.USER);

    mvc.perform(post("/api/v1/locations").header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isForbidden());

    verify(locations, times(1)).save(any(Location.class));
  }

  private User authenticateAs(UserRole role) {
    User caller = new User(7L, "alex", SUBJECT, role, null, null);
    // The application must use the database role, even if a token has a role claim.
    Jwt token = Jwt.withTokenValue("valid-token").header("alg", "RS256")
        .subject(SUBJECT).claim("role", "ADMIN").build();
    when(decoder.decode("valid-token")).thenReturn(token);
    when(users.findByGoogleSubject(SUBJECT)).thenReturn(Optional.of(caller));
    return caller;
  }
}
