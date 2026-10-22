package cst438.project2.backend.controller;

import cst438.project2.backend.dto.invite.MyInviteRequest;
import cst438.project2.backend.service.UserService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import cst438.project2.backend.dto.invite.InviteResponse;
import cst438.project2.backend.dto.invite.InviteRequest;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.dto.invite.EventInviteRequest;

@RestController
@RequestMapping("/api/v1/invites")
public class InviteController {

    private final InviteService inviteService;
    private final UserService userService;

    public InviteController(InviteService inviteService, UserService userService) {
        this.inviteService = inviteService;
        this.userService = userService;
    }

    // Gets a page of invitations
    @GetMapping
    public PagedModel<InviteResponse> getAllInvites(Pageable pageable) {
        return new PagedModel<>(inviteService.getInvites(pageable));
    }

    // Creates an invitation, provided a userId, and eventId
    @PostMapping
    public ResponseEntity<InviteResponse> createInvite(Authentication auth, @RequestBody InviteRequest request) {
        return created(inviteService.createInvite(auth, request));
    }

    @PostMapping("events/{eventId}")
    public ResponseEntity<InviteResponse> createEventInvite(Authentication auth,
        @PathVariable Long eventId, @RequestBody InviteRequest request) {
        return created(inviteService.createInvite(auth,
            new InviteRequest(request.userId(), eventId, request.status(), request.message())));
    }

    private ResponseEntity<InviteResponse> created(InviteResponse invite) {
        URI location = URI.create("/api/v1/invites/users/" + invite.userId() + "/events/" + invite.eventId());
        return ResponseEntity.created(location).body(invite);
    }

    // Updates invitation
    //      Obtains which one based on userID and eventId in request body
    @PutMapping
    public InviteResponse putInvite(Authentication auth, @RequestBody InviteRequest request) {
        return inviteService.updateInvite(auth, request);
    }

    // Partially updates invitation
    //      Obtains which one based on userID and eventId in request body
    @PatchMapping
    public InviteResponse patchInvite(Authentication auth, @RequestBody InviteRequest request) {
        return inviteService.patchInvite(auth, request);
    }

    //currently users would be able to change the userId, final route should have authentication to know that you are the user you are getting the invite lists for.
    // TODO: Make admin role only
    @GetMapping("users/{userId}")
    public PagedModel<InviteResponse> getUserInvites(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return new PagedModel<>(inviteService.getInvitesForUser(userId, pageable));
    }

    @GetMapping("events/{eventId}")
    public PagedModel<InviteResponse> getEventInvites(Authentication auth, @PathVariable Long eventId, Pageable pageable) {
        return new PagedModel<>(inviteService.getEventInvites(auth, eventId, pageable));
    }

    // creates invites for multiple users at one event
    @PostMapping("events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InviteResponse> createEventInvites(
            @PathVariable Long eventId,
            @RequestBody EventInviteRequest request
    ) {
        return inviteService.createEventInvites(eventId, request.userIds());
    }



    @GetMapping("users/{userId}/events/{eventId}")
    public InviteResponse getUserEventInvite(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return inviteService.getInvite(userId, eventId);
    }

    @PatchMapping("users/{userId}/events/{eventId}")
    public InviteResponse patchUserEventInvite(Authentication auth, @PathVariable Long userId,
        @PathVariable Long eventId, @RequestBody InviteRequest request) {
        return inviteService.patchInviteAsAdmin(auth, userId, eventId, request);
    }

    @DeleteMapping("users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserEventInvite(
            Authentication auth,
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        inviteService.deleteInvite(auth, userId, eventId);
    }

    @GetMapping("me")
    public Page<InviteResponse> getMyInvites(Authentication auth, Pageable pageable) {
        return inviteService.getMyInvites(auth,pageable);
    }

    @GetMapping("me/{eventId}")
    public InviteResponse getMyInvite(
            @PathVariable Long eventId,
            Authentication auth
    ) {
       return inviteService.getMyInvite(auth,eventId);
    }


    @PutMapping("me/{eventId}")
    public InviteResponse respondToMyInvite(
            @PathVariable Long eventId,
            @RequestBody MyInviteRequest request,
            Authentication auth
    ) {
        return inviteService.respondToMyInvite(auth, eventId, request);
    }
}
