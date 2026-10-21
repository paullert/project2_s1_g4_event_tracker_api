package cst438.project2.backend.controller;

import cst438.project2.backend.model.User;
import cst438.project2.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class OauthController {

  UserService userService;

  public OauthController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public AuthenticatedUser getCurrentUser(
      @AuthenticationPrincipal Jwt jwt,
      Authentication authentication
  ) {
    String role = authentication.getAuthorities()
        .stream()
        .map(authority -> authority.getAuthority())
        .filter(authority -> authority.startsWith("ROLE_"))
        .findFirst()
        .orElse(null);

    String subject = jwt.getSubject();

    User user = userService.findUserFromAuthentication(authentication);

    return new AuthenticatedUser(
        subject,
        user.getUsername(), //TODO: change to email once database is updated with email field
        jwt.getClaimAsString("name"),
        role
    );
  }

  public record AuthenticatedUser(
      String googleSubject,
      String email,
      String name,
      String role
  ) {
  }
}