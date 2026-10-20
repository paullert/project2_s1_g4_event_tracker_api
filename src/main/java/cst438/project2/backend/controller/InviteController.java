package cst438.project2.backend.controller;

import cst438.project2.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import cst438.project2.backend.dto.InviteResponse;
import cst438.project2.backend.dto.InviteResponsePatch;
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

  //TODO: Finish
  // Make admin role only
  @GetMapping
  public List<InviteResponse> getAllInvites() {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Not implemented yet"
    );
  }

  //TODO: Finish
  // Make admin role only
  @GetMapping("{inviteId}")
  public InviteResponse getInvite(@PathVariable Long inviteId) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Not implemented yet"
    );
  }

  //TODO: Finish
  // Make admin role only
  @PatchMapping("{inviteId}")
  public InviteResponse updateInvite(
      @PathVariable Long inviteId,
      @RequestBody(required = false) Object request
  ) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Not implemented yet"
    );
  }

  //TODO: Finish
  // Make admin role only
  @DeleteMapping("{inviteId}")
  public void deleteInvite(@PathVariable Long inviteId) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Not implemented yet"
    );
  }

  //currently users would be able to change the userId, final route should have authentication to know that you are the user you are getting the invite lists for.
  // TODO: Make admin role only
  @GetMapping("users/{userId}")
  public List<InviteResponse> getUserInvites(
      @PathVariable Long userId
  ) {
    return inviteService.getInvitesForUser(userId);
  }

  //TODO: Finish
  // TODO: Make user role only
  @GetMapping("me")
  public List<InviteResponse> getMyInvites() {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Not implemented yet"
    );
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
  @PatchMapping("me/{inviteId}")
  public InviteResponse respondToMyInvite(
      @PathVariable Long inviteId,
      @RequestBody InviteResponsePatch request
  ) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Authentication is not implemented yet"
    );
  }

  //TODO: Finish
  // make admin and manager role only
  @GetMapping("events/{eventId}")
  public List<InviteResponse> getEventInvites(@PathVariable Long eventId) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Authentication is not implemented yet"
    );
  }

  //TODO: Finish
  // make admin and manager role only
  @PostMapping("events/{eventId}")
  public InviteResponse createEventInvite(
      @PathVariable Long eventId,
      @RequestBody(required = false) Object request
  ) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED,
        "Authentication is not implemented yet"
    );
  }
}
