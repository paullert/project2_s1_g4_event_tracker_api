package cst438.project2.backend.controller;

import cst438.project2.backend.dto.UserResponse;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {


  private final UserService userService;

  public UserController(UserService userService){
    this.userService = userService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Page<UserResponse> getUsers(
      @RequestParam(defaultValue = "0") int page
  ) {
    return userService.getUsers(page).map( user -> new UserResponse(
        user.getUserId(), user.getUsername(), user.getRole()
    ));
  }
}
