package cst438.project2.backend.service;

import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.ManagerEvent;
import cst438.project2.backend.model.ManagerEventId;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.ManagerEventRepository;
import cst438.project2.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventOwnershipService {
  private final ManagerEventRepository managerEvents;
  private final EventRepository events;
  private final UserRepository users;

  public EventOwnershipService(ManagerEventRepository managerEvents,
      EventRepository events, UserRepository users) {
    this.managerEvents = managerEvents;
    this.events = events;
    this.users = users;
  }

  @Transactional(readOnly = true)
  public void requireCanManageEvent(User user, Long eventId) {
    if (eventId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventId is required");
    }
    if (user != null && (user.getRole() == UserRole.ADMIN
        || (user.getRole() == UserRole.MANAGER
            && managerEvents.existsById(new ManagerEventId(eventId, user.getUserId()))))) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "You are not a manager of this event");
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @Transactional
  public void assignCreator(User creator, Long eventId) {
    if (creator.getRole() == UserRole.MANAGER) {
      managerEvents.save(new ManagerEvent(eventId, creator.getUserId()));
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void assignManager(Long eventId, Long managerId) {
    requireEvent(eventId);
    User manager = users.findById(managerId)
        .orElseThrow(() -> new NotFoundException(managerId, "User"));
    if (manager.getRole() != UserRole.MANAGER) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Only users with the MANAGER role can be assigned to an event");
    }
    ManagerEventId id = new ManagerEventId(eventId, managerId);
    if (!managerEvents.existsById(id)) {
      managerEvents.save(new ManagerEvent(eventId, managerId));
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void removeManager(Long eventId, Long managerId) {
    requireEvent(eventId);
    managerEvents.deleteById(new ManagerEventId(eventId, managerId));
  }

  private void requireEvent(Long eventId) {
    if (!events.existsById(eventId)) {
      throw new NotFoundException(eventId, "Event");
    }
  }
}
