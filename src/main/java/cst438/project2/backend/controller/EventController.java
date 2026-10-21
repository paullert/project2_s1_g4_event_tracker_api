package cst438.project2.backend.controller;

import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.service.EventService;
import cst438.project2.backend.service.InviteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final InviteService inviteService;
    private final EventService eventService;

    public EventController(InviteService inviteService, EventService eventService) {
        this.inviteService = inviteService;
        this.eventService = eventService;
    }

    @GetMapping
    public Page<EventResponse> getEvents(Pageable pageable) {
        return eventService.getEvents(pageable);
    }

    @PostMapping
    public EventResponse createEvent(@RequestBody EventRequest eventRequest) {
        return eventService.createEvent(eventRequest);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId);
    }

    @PutMapping("/{eventId}")
    public EventResponse putEvent(@PathVariable Long eventId, @RequestBody EventRequest request) {
        return eventService.putEvent(eventId, request);
    }

    @PatchMapping("/{eventId}")
    public EventResponse patchEvent(@PathVariable Long eventId, @RequestBody EventRequest eventRequest) {
        return eventService.patchEvent(eventId, eventRequest);
    }

    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
    }

    @GetMapping("/{eventId}/totalAttendees")
    public long getEventTotalAttendees(@PathVariable Long eventId) {
        return inviteService.getAcceptedAttendeeCount(eventId);
    }
}
