package cst438.project2.backend.controller;

import cst438.project2.backend.dto.invite.MyInviteRequest;
import cst438.project2.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    //TODO: Finish
    // Make admin role only
    @GetMapping
    public Page<InviteResponse> getAllInvites(Pageable pageable) {
        return inviteService.getAllInvites(pageable);
    }

    // Creates an invitation, provided a userId, and eventId
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(@RequestBody InviteRequest request) {
        return inviteService.createInvite(request);
    }

    // Updates invitation
    //      Obtains which one based on userID and eventId in request body
    @PutMapping
    public InviteResponse putInvite(@RequestBody InviteRequest request) {
        return inviteService.updateInvite(request);
    }

    // Partially updates invitation
    //      Obtains which one based on userID and eventId in request body
    @PatchMapping
    public InviteResponse patchInvite(@RequestBody InviteRequest request) {
        return inviteService.patchInvite(request);
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
    public Page<InviteResponse> getEventInvites(@PathVariable Long eventId, Pageable pageable) {
        return inviteService.getEventInvites(eventId, pageable);
    }

    //TODO: Finish
    @GetMapping("users/{userId}/events/{eventId}")
    public InviteResponse getUserEventInvite(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return inviteService.getInvite(userId, eventId);
    }

    // TODO: Finish
    @DeleteMapping("users/{userId}/events/{eventId}")
    public InviteResponse deleteUserEventInvite(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return null;
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
