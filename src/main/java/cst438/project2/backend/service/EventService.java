package cst438.project2.backend.service;


import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // TODO: Finish this
    public Page<EventResponse> getEvents(Pageable pageable) {
        return null;
    }

    // TODO: Finish this
    public EventResponse getEvent(Long eventId) {
        return null;
    }

    // TODO: Finish this
    public void deleteEvent(Long eventId) {
        return;
    }

    // TODO: Finish this
    public EventResponse createEvent(EventResponse eventResponse) {
        return null;
    }

    // TODO: Finish this
    public EventResponse putEvent(Long eventId, EventResponse eventResponse) {

    }

    // TODO: Finish this
    public EventResponse patchEvent(Long eventId, EventResponse eventResponse) {

    }
}
