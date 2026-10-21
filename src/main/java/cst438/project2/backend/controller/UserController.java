package cst438.project2.backend.controller;

import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.model.User;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/users", "/users"})
public class UserController {


  private final UserService userService;

  public UserController(UserService userService){
    this.userService = userService;
  }

  @GetMapping
  public PagedModel<UserResponse> getUsers(
      Pageable pageable
  ) {
    return new PagedModel<>(userService.getUsers(pageable));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse createUser(@RequestBody UserRequest request) {
    return userService.createUser(request);
  }

  @GetMapping("/{userId}")
  public UserResponse getUser(@PathVariable Long userId) {
    return UserResponse.init(userService.getUser(userId));
  }

  @PatchMapping("/{userId}")
  public UserResponse updateUser(
      @PathVariable Long userId,
      @RequestBody UserRequest request
  ) {
    return UserResponse.init(userService.updateUser(userId, request));
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
  }

  @DeleteMapping("/me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMyAccount(Authentication authentication) {
    userService.deleteMe(authentication);
  }
}
