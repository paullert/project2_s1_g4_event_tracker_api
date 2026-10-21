package cst438.project2.backend.service;


import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // TODO: Finish this
    public Page<EventResponse> getEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
        return events.map(EventResponse::init);
    }

    // TODO: Second pass this
    public EventResponse getEvent(Long eventId) {
        return EventResponse.init(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(eventId, "Event")));
    }

    // TODO: Second pass this
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    // TODO: Second Pass this
    public EventResponse createEvent(EventRequest request) {
        Event event = new Event(
                null,
                request.eventName(),
                request.eventDescription(),
                request.startsOn(),
                request.creatorId(),
                null,
                request.locationId()
        );

        return EventResponse.init(eventRepository.save(event));
    }

    // TODO: Second Pass this
    public EventResponse putEvent(Long eventId, EventRequest request) {
        if ( request.eventDescription() == null ||
        request.eventName() == null ||
        request.locationId() == null ||
        request.startsOn() == null) {
            throw new BadRequestException("Missing 1 or more required fields: eventName, eventDescription, startsOn, creatorId, locationId");
        }
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

    // TODO: Second Pass this
    public EventResponse patchEvent(Long eventId, EventRequest request) {
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
