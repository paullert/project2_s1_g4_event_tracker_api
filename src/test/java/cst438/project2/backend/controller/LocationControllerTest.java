package cst438.project2.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  @BeforeEach
  void setUp() {
    location = new Location();
    location.setLocationId(7L);
    location.setLocationName("Main Office");
    location.setAddress("10 Main Street");
    location.setLocationDescription("Primary office");
    location.setUpdatedAt(LocalDateTime.parse("2026-01-01T12:00:00"));
  }

  @Test
  void getLocationReturnsLocation() throws Exception {
    when(locationService.getLocationById(7L)).thenReturn(location);

    mockMvc.perform(
            get("/api/v1/locations/7")
                .with(jwt())
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.locationId").value(7))
        .andExpect(jsonPath("$.locationName").value("Main Office"))
        .andExpect(jsonPath("$.address").value("10 Main Street"))
        .andExpect(jsonPath("$.locationDescription").value("Primary office"));
  }

  @Test
  void getLocationReturnsNotFoundWhenIdDoesNotExist() throws Exception {
    when(locationService.getLocationById(999L))
        .thenThrow(new NotFoundException(999L, "Location"));

    mockMvc.perform(
            get("/api/v1/locations/999")
                .with(jwt())
        )
        .andExpect(status().isNotFound())
        .andExpect(content().string("Location with id 999 not found"));
  }

  @Test
  void putLocationReplacesLocation() throws Exception {
    LocationCreate request = new LocationCreate(
        "Updated Office",
        "20 Main Street",
        "Updated description"
    );

    when(locationService.getLocationById(7L)).thenReturn(location);
    when(locationService.putLocation(any(Location.class), any(LocationCreate.class)))
        .thenReturn(location);

    mockMvc.perform(
            put("/api/v1/locations/7")
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
        .andExpect(jsonPath("$.location").value("/api/v1/locations/7"))
        .andExpect(jsonPath("$.object.locationId").value(7));
  }

  @Test
  void putLocationReturnsBadRequestWhenARequiredFieldIsMissing() throws Exception {
    when(locationService.getLocationById(7L)).thenReturn(location);
    when(locationService.putLocation(any(Location.class), any(LocationCreate.class)))
        .thenThrow(new BadRequestException(
            "Missing 1 or more required fields: locationName, address, locationDescription"));

    mockMvc.perform(
            put("/api/v1/locations/7")
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
        .andExpect(content().string(
            "Missing 1 or more required fields: locationName, address, locationDescription"));
  }

  @Test
  void putLocationReturnsNotFoundWhenIdDoesNotExist() throws Exception {
    when(locationService.getLocationById(999L))
        .thenThrow(new NotFoundException(999L, "Location"));

    mockMvc.perform(
            put("/api/v1/locations/999")
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
        .andExpect(content().string("Location with id 999 not found"));
  }
}
