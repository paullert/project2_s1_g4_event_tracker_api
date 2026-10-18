package cst438.project2.backend.controller;
import cst438.project2.backend.model.Invite;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
import cst438.project2.backend.model.User;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class InviteController {
    private final InviteService inviteService;
    private final UserService userService;

    public InviteController(InviteService inviteService, UserService userService){
        this.inviteService = inviteService;
        this.userService = userService;
    }
    @GetMapping("/me/invites")
    public List<InviteResponse> getMyInvites(
            @AuthenticationPrincipal Jwt jwt
    ) {
        User user = getAuthenticatedUser(jwt);

        return inviteService.getInvitesForUser(user.getUserId());
    }

    @PatchMapping("/me/invites/{inviteId}")
    public Invite respondToMyInvite(
            @PathVariable Long inviteId,
            @RequestBody InviteResponsePatch request,
            @AuthenticationPrincipal Jwt jwt
    ){
        User user = getAuthenticatedUser(jwt);

        return inviteService.updateInvite(
                inviteId, user.getUserId(),
                request.status()
        );
    }

    private User getAuthenticatedUser(Jwt jwt){
        if(jwt == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = jwt.getClaimAsString("email");

        if(email == null || email.isBlank()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email claim missing");
        }

        return userService.getUserByEmail(email);
    }
}
