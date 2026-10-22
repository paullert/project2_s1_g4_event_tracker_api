package cst438.project2.backend.controller;

import cst438.project2.backend.dto.user.RegisterRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.model.User;
import cst438.project2.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class OauthController {

  UserService userService;

  public OauthController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public UserResponse getCurrentUser(
      Authentication authentication
  ) {
    return UserResponse.init(userService.findUserFromAuthentication(authentication));
  }

  @PostMapping("/register")
  public UserResponse register(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody RegisterRequest request
  ) {
    User user = userService.registerUser(
        jwt,
        request.username()
    );

    return UserResponse.init(user);
  }
}