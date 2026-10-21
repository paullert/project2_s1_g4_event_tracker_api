package cst438.project2.backend.controller;

import static cst438.project2.backend.TestResources.Dates.NOW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static cst438.project2.backend.TestResources.LocationValues.*;
import static cst438.project2.backend.TestResources.ErrorValues.*;

import cst438.project2.backend.dto.location.LocationCreate;
import cst438.project2.backend.dto.location.LocationPatch;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.LocationService;
import cst438.project2.backend.service.UserService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LocationController.class)
@Import(SecurityConfig.class)
class LocationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LocationService locationService;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtAuthConverter jwtAuthConverter;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  private Location location;
  private final Long id = LOCATION_IDS[0];
  private final String name = LOCATION_NAMES[0];
  private final String address = ADDRESSES[0];
  private final String description = LOCATION_DESCRIPTIONS[0];
  private final LocalDateTime updatedAt = NOW;

  @BeforeEach
  void setUp() {
    location = new Location();
    location.setLocationId(id);
    location.setLocationName(name);
    location.setAddress(address);
    location.setLocationDescription(description);
    location.setUpdatedAt(updatedAt);
  }

  @Test
  void getLocationReturnsLocation() throws Exception {
    when(locationService.getLocationById(id)).thenReturn(location);

    mockMvc.perform(
            get("/api/v1/locations/%d".formatted(id))
                .with(jwt())
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.locationId").value(id))
        .andExpect(jsonPath("$.locationName").value(name))
        .andExpect(jsonPath("$.address").value(address))
        .andExpect(jsonPath("$.locationDesc").value(description));
  }

  @Test
  void listLocationsIsPublicAndUsesConfiguredDefaultSort() throws Exception {
    var pageable = PageRequest.of(0, 20, Sort.by("updatedAt").descending());
    when(locationService.getLocations(pageable)).thenReturn(new PageImpl<>(List.of(location)));
    mockMvc.perform(get("/api/v1/locations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].locationId").value(id));
    verify(locationService).getLocations(pageable);
  }

  @Test
  void createLocationReturns201WithLocationHeaderAndResource() throws Exception {
    when(locationService.createLocation(any(), any())).thenReturn(location);
    mockMvc.perform(post("/api/v1/locations").with(jwt())
        .contentType(MediaType.APPLICATION_JSON).content("""
            {"locationName":"Office","address":"10 Main Street","locationDesc":"Meeting room"}
            """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/locations/" + id))
        .andExpect(jsonPath("$.locationId").value(id))
        .andExpect(jsonPath("$.object").doesNotExist());
    verify(locationService).createLocation(eq(new LocationCreate("Office", "10 Main Street", "Meeting room")), any());
  }

  @Test
  void patchLocationBindsOnlySuppliedFields() throws Exception {
    when(locationService.getLocationById(id)).thenReturn(location);
    when(locationService.patchLocation(eq(location), any())).thenReturn(location);
    mockMvc.perform(patch("/api/v1/locations/" + id).with(jwt())
        .contentType(MediaType.APPLICATION_JSON).content("{\"address\":\"20 Main Street\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.locationId").value(id));
    verify(locationService).patchLocation(location, new LocationPatch(null, "20 Main Street", null));
  }

  @Test
  void deleteLocationReturns204WithoutABody() throws Exception {
    when(locationService.getLocationById(id)).thenReturn(location);
    mockMvc.perform(delete("/api/v1/locations/" + id).with(jwt()))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
    verify(locationService).deleteLocation(location);
  }

  @Test
  void locationWritesRequireAuthentication() throws Exception {
    mockMvc.perform(post("/api/v1/locations")
        .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(locationService, userService);
  }

  @Test
  void putLocationReplacesLocation() throws Exception {
    String apiAddress = "/api/v1/locations/%d".formatted(id);

    when(locationService.getLocationById(id)).thenReturn(location);
    when(locationService.putLocation(any(Location.class), any(LocationCreate.class)))
        .thenReturn(location);

    mockMvc.perform(
            put(apiAddress)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "locationName": "Updated Office",
                      "address": "20 Main Street",
                      "locationDescription": "Updated description"
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.locationId").value(id));
  }

  @Test
  void putLocationReturnsBadRequestWhenARequiredFieldIsMissing() throws Exception {
    String badReqMsg = constructBadRequestMessage("locationName, address");

    when(locationService.getLocationById(id)).thenReturn(location);
    when(locationService.putLocation(any(Location.class), any(LocationCreate.class)))
        .thenThrow(new BadRequestException(badReqMsg));

    mockMvc.perform(
            put("/api/v1/locations/%d".formatted(id))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "locationName": "Updated Office",
                      "address": "20 Main Street"
                    }
                    """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value(badReqMsg));
  }

}
