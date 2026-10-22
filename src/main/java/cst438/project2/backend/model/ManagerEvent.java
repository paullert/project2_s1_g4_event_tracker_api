package cst438.project2.backend.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "manager_event")
public class ManagerEvent {
  @EmbeddedId
  private ManagerEventId id;

  protected ManagerEvent() {}

  public ManagerEvent(Long eventId, Long managerId) {
    this.id = new ManagerEventId(eventId, managerId);
  }

  public ManagerEventId getId() {
    return id;
  }
}
