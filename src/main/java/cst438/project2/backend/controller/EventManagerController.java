package cst438.project2.backend.controller;

import cst438.project2.backend.service.EventOwnershipService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events/{eventId}/managers")
public class EventManagerController {
  private final EventOwnershipService ownership;

  public EventManagerController(EventOwnershipService ownership) {
    this.ownership = ownership;
  }

  @PutMapping("/{managerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignManager(@PathVariable Long eventId, @PathVariable Long managerId) {
    ownership.assignManager(eventId, managerId);
  }

  @DeleteMapping("/{managerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeManager(@PathVariable Long eventId, @PathVariable Long managerId) {
    ownership.removeManager(eventId, managerId);
  }
}
