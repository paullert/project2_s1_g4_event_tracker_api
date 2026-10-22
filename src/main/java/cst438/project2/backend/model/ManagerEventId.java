package cst438.project2.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ManagerEventId implements Serializable {
  @Column(name = "event_id")
  private Long eventId;

  @Column(name = "manager_id")
  private Long managerId;

  protected ManagerEventId() {}

  public ManagerEventId(Long eventId, Long managerId) {
    this.eventId = eventId;
    this.managerId = managerId;
  }

  public Long getEventId() {
    return eventId;
  }

  public Long getManagerId() {
    return managerId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof ManagerEventId that)) return false;
    return Objects.equals(eventId, that.eventId)
        && Objects.equals(managerId, that.managerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, managerId);
  }
}
