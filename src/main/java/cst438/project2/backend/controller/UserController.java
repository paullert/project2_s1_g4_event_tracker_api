package cst438.project2.backend.controller;

import cst438.project2.backend.service.UserService;
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
public class UserController {
  private final UserService userService;

  public UserController(UserService userService){
    this.userService = userService;
  }
}
