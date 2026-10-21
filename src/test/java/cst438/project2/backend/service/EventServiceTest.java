package cst438.project2.backend.service;

import static cst438.project2.backend.TestResources.Dates.OFFSET_TIMES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static cst438.project2.backend.TestResources.EventValues.*;
import static cst438.project2.backend.TestResources.UserValues.*;
import static cst438.project2.backend.TestResources.LocationValues.*;
import static cst438.project2.backend.TestResources.ErrorValues.*;

import cst438.project2.backend.dto.ApiDates;
import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.InviteRepository;
import cst438.project2.backend.repository.ManagerEventRepository;
import org.springframework.security.core.Authentication;
import cst438.project2.backend.security.SecurityConfig;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private UserService userService;

  @Mock private Authentication authentication;
  @Mock private EventOwnershipService ownership;
  @Mock private ManagerEventRepository managerEvents;
  @Mock private InviteRepository invites;

  @InjectMocks
  private EventService eventService;

  private final LocalDateTime now = NOW;

  // Used to format the date according to the response format
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


  private Event event;
  private final Long eventId = EVENT_IDS[0];
  private final String eventName = BASE_EVENT_NAMES[0];
  private final String eventDescription = BASE_EVENT_DESCRIPTIONS[0];
  private final LocalDateTime eventStartTime = EVENT_START_TIMES[0];
  private final Long creatorId = CREATOR_IDS[0];
  private final Long locationId = EVENT_LOCATION_IDS[0];


  private final User creator = new User(
      creatorId,
      USERNAMES[1],
      PHONE_NUMBERS[1],
      ROLES[0],
      now,
      now
  );

  private final Location location = new Location(
      locationId,
      LOCATION_NAMES[2],
      ADDRESSES[2],
      LOCATION_DESCRIPTIONS[2],
      creator,
      now,
      now
  );

  @BeforeEach
  void setUp() {
    event = new Event(
        eventId,
        eventName,
        eventDescription,
        eventStartTime,
        creatorId,
        eventStartTime,
        location.getLocationId()
    );
  }

  @Test
  void putEventReplacesAllFields() {
    String name = EVENT_NAMES[0];
    String description = EVENT_DESCRIPTIONS[0];
    LocalDateTime newStartTime = OFFSET_TIMES[0];
    Long newCreatorId = UPDATED_CREATOR_IDS[0];
    Long newLocationId = UPDATED_LOCATION_IDS[0];

    EventRequest request = new EventRequest(
        name,
        description,
        newStartTime,
        newCreatorId,
        newLocationId
    );

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenReturn(event);

    EventResponse result = eventService.putEvent(authentication, eventId, request);

    assertEquals(name, result.eventName());
    assertEquals(description, result.eventDescription());
    assertEquals(ApiDates.format(newStartTime), result.startsOn());
    assertEquals(newCreatorId, result.creatorId());
    assertEquals(newLocationId, result.locationId());

    verify(eventRepository).save(event);
  }

  @Test
  void patchEventReplacesOnlySuppliedFields() {
    String name1 = UPDATE_EVENT_NAMES[0];
    String name2 = UPDATE_EVENT_NAMES[1];
    String description1 = UPDATE_EVENT_DESCRIPTIONS[0];
    String description2 = UPDATE_EVENT_DESCRIPTIONS[1];

    LocalDateTime newStartTime = OFFSET_TIMES[0];
    Long newCreatorId = UPDATED_CREATOR_IDS[0];
    Long newLocationId = UPDATED_LOCATION_IDS[0];

    EventRequest request1 = new EventRequest(
        name1,
        description1,
        null,
        null,
        null
    );

    EventRequest request2 = new EventRequest(
        name2,
        null,
        newStartTime,
        null,
        null
    );

    EventRequest request3 = new EventRequest(
        null,
        description2,
        null,
        newCreatorId,
        newLocationId
    );


    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenReturn(event);

    EventResponse result1 = eventService.patchEvent(authentication, eventId, request1);
    assertEquals(name1, result1.eventName());
    assertEquals(description1, result1.eventDescription());
    assertEquals(ApiDates.format(eventStartTime), result1.startsOn());
    assertEquals(creatorId, result1.creatorId());
    assertEquals(locationId, result1.locationId());

    EventResponse result2 = eventService.patchEvent(authentication, eventId, request2);
    assertEquals(name2, result2.eventName());
    assertEquals(description1, result2.eventDescription());
    assertEquals(ApiDates.format(newStartTime), result2.startsOn());
    assertEquals(creatorId, result2.creatorId());
    assertEquals(locationId, result2.locationId());

    EventResponse result3 = eventService.patchEvent(authentication, eventId, request3);
    assertEquals(name2, result3.eventName());
    assertEquals(description2, result3.eventDescription());
    assertEquals(ApiDates.format(newStartTime), result3.startsOn());
    assertEquals(newCreatorId, result3.creatorId());
    assertEquals(newLocationId, result3.locationId());

    verify(eventRepository, times(3)).save(event);
  }

  @Test
  void missingEventThrowsNotFound() {
    when(eventRepository.findById(INVALID_REQUEST_ID))
        .thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(
        ResponseStatusException.class,
        () -> eventService.getEvent(INVALID_REQUEST_ID)
    );

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    verify(eventRepository, never()).save(any());
  }

  @Test
  void incompleteEventPutThrowsBadRequest() {
    // All bad requests should throw BadRequestException with the following message
    String badReqMsg = constructBadRequestMessage("eventName, eventDescription, startsOn, creatorId, locationId");

    EventRequest request1 = new EventRequest(
        null,
        INVALID_REQUEST_STRING,
        null,
        null,
        null
    );

    EventRequest request2 = new EventRequest(
        INVALID_REQUEST_STRING,
        null,
        INVALID_REQUEST_DATE,
        null,
        null
    );

    EventRequest request3 = new EventRequest(
        null,
        null,
        null,
        INVALID_REQUEST_ID,
        INVALID_REQUEST_ID
    );

    BadRequestException ex1 = assertThrows(
        BadRequestException.class,
        () -> eventService.putEvent(authentication, eventId, request1)
    );
    assertEquals(badReqMsg, ex1.getMessage());

    BadRequestException ex2 = assertThrows(
        BadRequestException.class,
        () -> eventService.putEvent(authentication, eventId, request2)
    );
    assertEquals(badReqMsg, ex2.getMessage());

    BadRequestException ex3 = assertThrows(
        BadRequestException.class,
        () -> eventService.putEvent(authentication, eventId, request3)
    );
    assertEquals(badReqMsg, ex3.getMessage());

    // Verify no fields changed during the failed puts
    assertEquals(eventName, event.getEventName());
    assertEquals(eventDescription, event.getEventDescription());
    assertEquals(eventStartTime, event.getStartsOn());
    assertEquals(creatorId, event.getCreatorId());
    assertEquals(locationId, event.getLocationId());
    assertEquals(now, event.getUpdatedAt());

    // Verify no database calls were made
    verify(eventRepository, never()).save(any());
  }
}
