package cst438.project2.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import cst438.project2.backend.dto.InviteResponse;
import cst438.project2.backend.dto.InviteResponsePatch;
import cst438.project2.backend.service.InviteService;

@RestController
@RequestMapping("/api/v1/users")
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService){
        this.inviteService = inviteService;
    }

    //currently users would be able to change the userId, final route should have authentication to know that you are the user you are getting the invite lists for.
    @GetMapping("/{userId}/invites")
    public List<InviteResponse> getUserInvites(
            @PathVariable Long userId
    ) {
        return inviteService.getInvitesForUser(userId);
    }

    @PatchMapping("/me/invites/{inviteId}")
    public void respondToMyInvite(
            @PathVariable Long inviteId,
            @RequestBody InviteResponsePatch request
    ){
        //authentication not implemented yet
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Authentication is not implemented yet"
        );
    }
}
