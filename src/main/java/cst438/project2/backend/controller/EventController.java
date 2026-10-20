package cst438.project2.backend.controller;

import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.service.EventService;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final InviteService inviteService;
    private final EventService eventService;

    public EventController(InviteService inviteService, EventService eventService) {
        this.inviteService = inviteService;
        this.eventService = eventService;
    }

    // TODO: Finish this
    @GetMapping
    public Page<EventResponse> getEvents(Pageable pageable) {
        return eventService.getEvents(pageable);
    }

    // TODO: Finish this
    @PostMapping
    public EventResponse createEvent(EventRequest eventRequest) {
        return eventService.createEvent(eventRequest);
    }

    // TODO: Finish this
    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId);
    }

    // TODO: Finish this
    @PutMapping("/{eventId}")
    public EventResponse putEvent(@PathVariable Long eventId, EventRequest request) {
        return eventService.putEvent(eventId, request);
    }

    // TODO: Finish this
    @PatchMapping("/{eventId}")
    public EventResponse patchEvent(@PathVariable Long eventId, EventRequest eventRequest) {
        return eventService.patchEvent(eventId, eventRequest);
    }

    // TODO: Finish this
    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return;
    }

    @GetMapping("/{eventId}/totalAttendees")
    public long getEventTotalAttendees(@PathVariable Long eventId) {
        return inviteService.getAcceptedAttendeeCount(eventId);
    }
}