package cst438.project2.backend.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Used to create composite primary key for invite data model
@Embeddable
public class InviteId {
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
}
