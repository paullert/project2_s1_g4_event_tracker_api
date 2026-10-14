package cst438.project2.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import cst438.project2.backend.dto.InviteResponse;
import cst438.project2.backend.model.Invite;
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
}
