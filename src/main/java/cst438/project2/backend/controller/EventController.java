package cst438.project2.backend.controller;

import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.service.InviteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final InviteService inviteService;

    public EventController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    // TODO: Finish this
    @GetMapping
    public Page<EventResponse> getEvents(Pageable pageable) {
        return null;
    }

    // TODO: Finish this
    @PostMapping
    public EventResponse createEvent(EventRequest eventRequest) {
        return null;
    }

    // TODO: Finish this
    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return null;
    }

    // TODO: Finish this
    @PutMapping("/{eventId}")
    public EventResponse putEvent(@PathVariable Long eventId, EventRequest eventRequest) {
        return null;
    }

    // TODO: Finish this
    @PatchMapping("/{eventId}")
    public EventResponse patchEvent(@PathVariable Long eventId, EventRequest eventRequest) {
        return null;
    }

    // TODO: Finish this
    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable Long eventId) {
        return;
    }

    @GetMapping("/{eventId}/totalAttendees")
    public long getEventTotalAttendees(@PathVariable Long eventId) {
        return inviteService.getAcceptedAttendeeCount(eventId);
    }
}