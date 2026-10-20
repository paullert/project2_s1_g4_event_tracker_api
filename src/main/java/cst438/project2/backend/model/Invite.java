package cst438.project2.backend.model;

import jakarta.persistence.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
public class Invite {

    // Since the id is composite, we need to use EmbeddedId
    //   InviteId contains "userId" = "user_id" and "eventId" = "event_id"
    @EmbeddedId
    private InviteId inviteId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "course_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "status")
    private String status;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public Invite(){}

    public Invite(Long inviteId, User user, Event event, String status, LocalDateTime invitedAt, LocalDateTime respondedAt) {
        this.inviteId = inviteId;
        this.user = user;
        this.event = event;
        this.status = status;
        this.invitedAt = invitedAt;
        this.respondedAt = respondedAt;
    }

    public InviteId getInviteId() {
        return inviteId;
    }

    public void setInviteId(InviteId inviteId) {
        this.inviteId = inviteId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
