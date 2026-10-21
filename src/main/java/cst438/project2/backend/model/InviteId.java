package cst438.project2.backend.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// Used to create composite primary key for invite data model
@Embeddable
public class InviteId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "event_id")
    private Long eventId;

    public InviteId(Long userId, Long eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    public InviteId() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof InviteId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, eventId);
    }
}
