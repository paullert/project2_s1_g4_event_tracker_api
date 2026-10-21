package cst438.project2.backend.controller;

import static cst438.project2.backend.TestResources.Dates.NOW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static cst438.project2.backend.TestResources.LocationValues.*;
import static cst438.project2.backend.TestResources.ErrorValues.*;

import cst438.project2.backend.dto.location.LocationCreate;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
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
        .andExpect(jsonPath("$.locationDescription").value(description));
  }

  @Test
  void getLocationReturnsNotFoundWhenIdDoesNotExist() throws Exception {
    // All not found requests should throw NotFoundException with a message like:
    String notFoundMsg = constructNotFoundMessage("Location", "id "+ INVALID_REQUEST_ID);
    when(locationService.getLocationById(INVALID_REQUEST_ID))
        .thenThrow(new NotFoundException(INVALID_REQUEST_ID, "Location"));

    mockMvc.perform(
            get("/api/v1/locations/%d".formatted(INVALID_REQUEST_ID))
                .with(jwt())
        )
        .andExpect(status().isNotFound())
        .andExpect(content().string(notFoundMsg));
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
        .andExpect(jsonPath("$.status").value("Successful"))
        .andExpect(jsonPath("$.message").value("Location Updated (Put)"))
        .andExpect(jsonPath("$.location").value(apiAddress))
        .andExpect(jsonPath("$.object.locationId").value(id));
  }

  @Test
  void putLocationReturnsBadRequestWhenARequiredFieldIsMissing() throws Exception {
    String badReqMsg = constructBadRequestMessage("locationName, address, locationDescription");

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
        .andExpect(content().string(badReqMsg));
  }

  @Test
  void putLocationReturnsNotFoundWhenIdDoesNotExist() throws Exception {
    String apiAddress = "/api/v1/locations/%d".formatted(INVALID_REQUEST_ID);
    String notFoundMsg = constructNotFoundMessage("Location", "id "+ INVALID_REQUEST_ID);
    when(locationService.getLocationById(INVALID_REQUEST_ID))
        .thenThrow(new NotFoundException(INVALID_REQUEST_ID, "Location"));

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
        .andExpect(status().isNotFound())
        .andExpect(content().string(notFoundMsg));
  }
}
