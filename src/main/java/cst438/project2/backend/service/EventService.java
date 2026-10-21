package cst438.project2.backend.service;


import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.InviteRepository;
import cst438.project2.backend.repository.ManagerEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

  private final EventRepository eventRepository;
  private final UserService userService;
  private final EventOwnershipService ownership;
  private final ManagerEventRepository managerEvents;
  private final InviteRepository invites;

  public EventService(EventRepository eventRepository, UserService userService,
      EventOwnershipService ownership, ManagerEventRepository managerEvents,
      InviteRepository invites) {
    this.eventRepository = eventRepository;
    this.userService = userService;
    this.ownership = ownership;
    this.managerEvents = managerEvents;
    this.invites = invites;
  }

  @PreAuthorize("hasAnyRole('USER','MANAGER', 'ADMIN')")
  @Transactional(readOnly = true)
  public Page<EventResponse> getEvents(Pageable pageable) {
    Page<Event> events = eventRepository.findAll(pageable);
    return events.map(EventResponse::init);
  }

  @PreAuthorize("hasAnyRole('USER','MANAGER', 'ADMIN')")
  @Transactional(readOnly = true)
  public EventResponse getEvent(Long eventId) {
    return EventResponse.init(eventRepository.findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found")));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @Transactional
  public void deleteEvent(Authentication auth, Long eventId) {
    User user = userService.findUserFromAuthentication(auth);
    ownership.requireCanManageEvent(user, eventId);
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException(eventId, "Event"));
    invites.deleteForEvent(eventId);
    managerEvents.deleteForEvent(eventId);
    eventRepository.delete(event);
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @Transactional
  public EventResponse createEvent(Authentication auth, EventRequest request) {
    if (request.eventName() == null || request.eventDescription() == null || request.startsOn() == null) {
      throw new BadRequestException("Missing required fields: eventName, eventDescription, startsOn");
    }

    User user = userService.findUserFromAuthentication(auth);
    Event event = new Event(
        null,
        request.eventName(),
        request.eventDescription(),
        request.startsOn(),
        user.getUserId(),
        null,
        request.locationId()
    );

    event = eventRepository.save(event);
    ownership.assignCreator(user, event.getId());
    return EventResponse.init(event);
  }

  // TODO: Second Pass this
  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @Transactional
  public EventResponse putEvent(Authentication auth, Long eventId, EventRequest request) {
    if (request.eventDescription() == null ||
        request.eventName() == null ||
        request.locationId() == null ||
        request.startsOn() == null) {
      throw new BadRequestException(
          "Missing 1 or more required fields: eventName, eventDescription, startsOn, creatorId, locationId");
    }
    User user = userService.findUserFromAuthentication(auth);
    ownership.requireCanManageEvent(user, eventId);

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException(eventId, "Event"));
    event.setEventName(request.eventName());
    event.setEventDescription(request.eventDescription());
    event.setStartsOn(request.startsOn());
    if (request.creatorId() != null) {
      event.setCreatorId(request.creatorId());
    }
    event.setLocationId(request.locationId());
    return EventResponse.init(eventRepository.save(event));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @Transactional
  public EventResponse patchEvent(Authentication auth, Long eventId, EventRequest request) {
    User user = userService.findUserFromAuthentication(auth);
    ownership.requireCanManageEvent(user, eventId);

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException(eventId, "Event"));
    if (request.eventName() != null) {
      event.setEventName(request.eventName());
    }
    if (request.eventDescription() != null) {
      event.setEventDescription(request.eventDescription());
    }
    if (request.startsOn() != null) {
      event.setStartsOn(request.startsOn());
    }
    if (request.locationId() != null) {
      event.setLocationId(request.locationId());
    }
    if (request.creatorId() != null) {
      event.setCreatorId(request.creatorId());
    }
    return EventResponse.init(eventRepository.save(event));
  }
}
