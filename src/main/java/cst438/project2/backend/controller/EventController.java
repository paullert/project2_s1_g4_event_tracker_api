package cst438.project2.backend.controller;

import cst438.project2.backend.dto.event.EventRequest;
import cst438.project2.backend.dto.event.EventResponse;
import cst438.project2.backend.service.EventService;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import org.springframework.security.core.Authentication;
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
    public PagedModel<EventResponse> getEvents(
        @PageableDefault(size = 20, sort = "startsOn", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(eventService.getEvents(pageable));
    }

    // TODO: Finish this
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(Authentication auth, @RequestBody EventRequest eventRequest) {
        EventResponse event = eventService.createEvent(auth, eventRequest);
        return ResponseEntity.created(URI.create("/api/v1/events/" + event.eventId())).body(event);
    }

    // TODO: Finish this
    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId);
    }

    // TODO: Finish this
    @PutMapping("/{eventId}")
    public EventResponse putEvent(Authentication auth, @PathVariable Long eventId, @RequestBody EventRequest request) {
        return eventService.putEvent(auth, eventId, request);
    }

    // TODO: Finish this
    @PatchMapping("/{eventId}")
    public EventResponse patchEvent(Authentication auth, @PathVariable Long eventId, @RequestBody EventRequest eventRequest) {
        return eventService.patchEvent(auth, eventId, eventRequest);
    }

    // TODO: Finish this
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(Authentication auth, @PathVariable Long eventId) {
        eventService.deleteEvent(auth, eventId);
    }

    @GetMapping("/{eventId}/totalAttendees")
    public long getEventTotalAttendees(@PathVariable Long eventId,
        @RequestParam(defaultValue = "false") boolean archived) {
        return archived ? inviteService.getArchivedAttendeeCount(eventId)
            : inviteService.getAcceptedAttendeeCount(eventId);
    }
}
