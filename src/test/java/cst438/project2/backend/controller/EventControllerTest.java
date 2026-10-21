package cst438.project2.backend.controller;

import static cst438.project2.backend.TestResources.Dates.OFFSET_TIMES;
import static cst438.project2.backend.TestResources.ErrorValues.INVALID_REQUEST_ID;
import static cst438.project2.backend.TestResources.ErrorValues.constructBadRequestMessage;
import static cst438.project2.backend.TestResources.ErrorValues.constructNotFoundMessage;
import static cst438.project2.backend.TestResources.EventValues.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.EventService;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.LocationService;
import cst438.project2.backend.service.UserService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@Import(SecurityConfig.class)
class EventControllerTest {

  @Autowired
  private MockMvc mockMvc;

  // Events controller = event service
  @MockitoBean
  private EventService eventService;

  // All Events have a location, so we need a location service
  @MockitoBean
  private LocationService locationService;

  // All Events have a creator, so we need a user service
  @MockitoBean
  private UserService userService;

  // Required for GET getEventTotalAttendees
  @MockitoBean
  private InviteService inviteService;

  @MockitoBean
  private JwtAuthConverter jwtAuthConverter;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  private Event event; // Used in every function

  // Initialize values, non-needed but makes it easier to read
  //    Values are from TestResources, see that file for more info
  private final Long eventId = EVENT_IDS[0];
  private final String eventName = BASE_EVENT_NAMES[0];
  private final String eventName2 = BASE_EVENT_NAMES[1];
  private final String eventDescription = BASE_EVENT_DESCRIPTIONS[0];
  private final String eventDescription2 = BASE_EVENT_DESCRIPTIONS[1];
  private final LocalDateTime now = NOW;
  private final LocalDateTime eventStartTime = OFFSET_TIMES[0];
  private final LocalDateTime eventStartTime2 = OFFSET_TIMES[1];
  private final Long creatorId = CREATOR_IDS[0];
  private final Long locationId = EVENT_LOCATION_IDS[0];
  private final String badReqMsg = constructBadRequestMessage("eventName, eventDescription, startsOn, creatorId, locationId");
  private final String notFoundMsg = constructNotFoundMessage("Event", "id "+ INVALID_REQUEST_ID);


  @BeforeEach
  void setUp() {
    event = new Event(
        eventId,
        eventName,
        eventDescription,
        eventStartTime,
        creatorId,
        now,
        locationId
    );
  }

  @Test
  void getEventReturnsEvent() throws Exception {
    when(eventService.getEvent(eventId)).thenReturn(EventResponse.init(event));

    mockMvc.perform(
            get("/api/v1/events/%d".formatted(eventId))
                .with(jwt())
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.eventId").value(eventId))
        .andExpect(jsonPath("$.eventName").value(eventName))
        .andExpect(jsonPath("$.eventDescription").value(eventDescription))
        .andExpect(jsonPath("$.startsOn").value(eventStartTime
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
        .andExpect(jsonPath("$.creatorId").value(creatorId))
        .andExpect(jsonPath("$.locationId").value(locationId));
  }

  @Test
  void listEventsBindsPaginationAndReturnsPage() throws Exception {
    var pageable = PageRequest.of(0, 2);
    when(eventService.getEvents(pageable)).thenReturn(
        new PageImpl<>(List.of(EventResponse.init(event)), pageable, 1));
    mockMvc.perform(get("/api/v1/events?size=2").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].eventId").value(eventId))
        .andExpect(jsonPath("$.totalElements").value(1));
    verify(eventService).getEvents(pageable);
  }

  @Test
  void createEventBindsJsonAndPassesAuthenticatedCaller() throws Exception {
    when(eventService.createEvent(any(), any())).thenReturn(EventResponse.init(event));
    mockMvc.perform(post("/api/v1/events")
        .with(jwt().jwt(token -> token.subject("manager-subject")))
        .contentType(MediaType.APPLICATION_JSON).content("""
            {"eventName":"New event","eventDescription":"Planning",
             "startsOn":"2026-11-01T12:00:00","locationId":1}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eventId").value(eventId));
    verify(eventService).createEvent(
        argThat(auth -> "manager-subject".equals(auth.getName())),
        eq(new EventRequest("New event", "Planning", LocalDateTime.of(2026, 11, 1, 12, 0), null, 1L)));
  }

  @Test
  void patchEventPassesOnlySuppliedFields() throws Exception {
    when(eventService.patchEvent(any(), eq(eventId), any())).thenReturn(EventResponse.init(event));
    mockMvc.perform(patch("/api/v1/events/" + eventId).with(jwt())
        .contentType(MediaType.APPLICATION_JSON).content("{\"eventName\":\"Renamed\"}"))
        .andExpect(status().isOk());
    verify(eventService).patchEvent(any(), eq(eventId),
        eq(new EventRequest("Renamed", null, null, null, null)));
  }

  @Test
  void deleteEventPassesPathIdAndAuthentication() throws Exception {
    mockMvc.perform(delete("/api/v1/events/" + eventId)
        .with(jwt().jwt(token -> token.subject("manager-subject"))))
        .andExpect(status().isOk()).andExpect(content().string(""));
    verify(eventService).deleteEvent(argThat(auth -> "manager-subject".equals(auth.getName())), eq(eventId));
  }

  @ParameterizedTest
  @ValueSource(longs = {0, 12})
  void attendeeCountIsPublicAndReturnsNumericJson(long count) throws Exception {
    when(inviteService.getAcceptedAttendeeCount(eventId)).thenReturn(count);
    mockMvc.perform(get("/api/v1/events/" + eventId + "/totalAttendees"))
        .andExpect(status().isOk()).andExpect(content().string(Long.toString(count)));
    verify(inviteService).getAcceptedAttendeeCount(eventId);
  }

  @ParameterizedTest
  @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
  void eventWritesRequireAuthentication(String method) throws Exception {
    String path = method.equals("POST") ? "/api/v1/events" : "/api/v1/events/7";
    mockMvc.perform(request(HttpMethod.valueOf(method), path)
        .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(eventService);
  }

  @Test
  void createEventRequiresRequestBody() throws Exception {
    mockMvc.perform(post("/api/v1/events").with(jwt()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(eventService);
  }

  @Test
  void nonNumericEventIdReturns400() throws Exception {
    mockMvc.perform(get("/api/v1/events/not-a-number").with(jwt()))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(eventService);
  }

  @Test
  void getEventReturnsNotFoundWhenIdDoesNotExist() throws Exception {
    when(eventService.getEvent(INVALID_REQUEST_ID))
        .thenThrow(new NotFoundException(INVALID_REQUEST_ID, "Event"));

    mockMvc.perform(
            get("/api/v1/events/%d".formatted(INVALID_REQUEST_ID))
                .with(jwt())
        )
        .andExpect(status().isNotFound())
        .andExpect(content().string(notFoundMsg));
  }

  @Test
  void putEventReplacesEvent() throws Exception {

    when(eventService.putEvent(any(Authentication.class), any(Long.class), any(EventRequest.class)))
        .thenReturn(EventResponse.init(event));

    mockMvc.perform(
            put(String.format("/api/v1/events/%d", eventId))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventId": %d,
                      "eventName": "%s",
                      "eventDescription": "%s",
                      "startsOn": "%s",
                      "creatorId": %d,
                      "locationId": %d
                    }
                    """.formatted(eventId, eventName, eventDescription,
                    eventStartTime.toString(), creatorId, locationId))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.eventId").value(eventId))
        .andExpect(jsonPath("$.eventName").value(eventName))
        .andExpect(jsonPath("$.eventDescription").value(eventDescription))
        .andExpect(jsonPath("$.startsOn").value(eventStartTime
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))) // Desired format, was ISO format
        .andExpect(jsonPath("$.creatorId").value(creatorId))
        .andExpect(jsonPath("$.locationId").value(locationId));
  }

  @Test
  void putEventReturnsBadRequestWhenARequiredFieldIsMissing() throws Exception {
    when(eventService.putEvent(any(Authentication.class), any(Long.class), any(EventRequest.class)))
        .thenThrow(new BadRequestException(badReqMsg));

    mockMvc.perform(
            put("/api/v1/events/%d".formatted(eventId))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventId": %d,
                      "eventName": %s,
                      "eventDescription": "%s",
                      "startsOn": "%s",
                      "creatorId": %d,
                      "locationId": %d
                    }
                    """.formatted(eventId, null, eventDescription,
                    eventStartTime.toString(), creatorId, locationId))
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().string(badReqMsg));
  }

  @Test
  void putEventReturnsNotFoundWhenIdDoesNotExist() throws Exception {
    when(eventService.putEvent(any(Authentication.class), any(Long.class), any(EventRequest.class)))
        .thenThrow(new NotFoundException(INVALID_REQUEST_ID, "Event"));

    mockMvc.perform(
            put("/api/v1/events/%d".formatted(INVALID_REQUEST_ID))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventName": "%s",
                      "eventDescription": "%s",
                      "startsOn": "%s",
                      "creatorId": %d,
                      "locationId": %d
                    }
                    """.formatted(eventName, eventDescription,
                    eventStartTime.toString(), creatorId, locationId))
        )
        .andExpect(status().isNotFound())
        .andExpect(content().string(notFoundMsg));
  }
}
