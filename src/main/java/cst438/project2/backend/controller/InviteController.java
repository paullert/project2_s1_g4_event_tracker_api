package cst438.project2.backend.controller;

import cst438.project2.backend.dto.invite.MyInviteRequest;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import cst438.project2.backend.dto.invite.InviteResponse;
import cst438.project2.backend.dto.invite.InviteRequest;
import cst438.project2.backend.service.InviteService;

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
    public Page<InviteResponse> getAllInvites(Pageable pageable) {
        return inviteService.getInvites(pageable);
    }

    // Creates an invitation, provided a userId, and eventId
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(Authentication auth, @RequestBody InviteRequest request) {
        return inviteService.createInvite(auth, request);
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
    public Page<InviteResponse> getUserInvites(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return inviteService.getInvitesForUser(userId, pageable);
    }

    //TODO: Finish
    // make admin and manager role only
    @GetMapping("events/{eventId}")
    public Page<InviteResponse> getEventInvites(Authentication auth, @PathVariable Long eventId, Pageable pageable) {
        return inviteService.getEventInvites(auth, eventId, pageable);
    }

    //TODO: Finish
    @GetMapping("users/{userId}/events/{eventId}")
    public InviteResponse getUserEventInvite(
            Authentication auth,
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return inviteService.getInvite(auth, userId, eventId);
    }

    // TODO: Finish
    @DeleteMapping("users/{userId}/events/{eventId}")
    public InviteResponse deleteUserEventInvite(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return null;
    }

    //TODO: Finish
    // TODO: Make user role only
    @GetMapping("me")
    public Page<InviteResponse> getMyInvites(Authentication auth, Pageable pageable) {
        Page<InviteResponse> invites = inviteService.getMyInvites(auth, pageable);
        return invites;
    }

    //TODO: Finish
    // make user role only
    @GetMapping("me/{inviteId}")
    public InviteResponse getMyInvite(
            @PathVariable Long inviteId,
            @RequestBody(required = false) Object request
    ) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Authentication is not implemented yet"
        );
    }


    //TODO: Finish
    // make user role only
    @PutMapping("me/{inviteId}")
    public InviteResponse respondToMyInvite(
            @PathVariable Long inviteId,
            @RequestBody MyInviteRequest request
    ) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Authentication is not implemented yet"
        );
    }
}
