package cst438.project2.backend.controller;

import cst438.project2.backend.service.InviteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final InviteService inviteService;

    public EventController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @GetMapping("/{eventId}/totalAttendees")
    public long getEventTotalAttendees(@PathVariable Long eventId) {
        return inviteService.getAcceptedAttendeeCount(eventId);
    }
}