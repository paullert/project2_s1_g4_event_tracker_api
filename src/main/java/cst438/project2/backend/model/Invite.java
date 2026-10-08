package cst438.project2.backend.model;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invites")
public class Invite {
    private String status;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public Invite(){

    }

    public Long getId(){
        return id;
    }

    public Long getUserId(){
        return userId;
    }

    public Long getEventId(){
        return eventId;
    }

    public String getStatus(){
        return status;
    }

    public LocalDateTime getInvitedAt(){
        return invitedAt;
    }

    public LocalDateTime getRespondedAt(){
        return respondedAt;
    }

    //tells the api that invites.event_id is referencing the events.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;

    public Event getEvent(){
        return event;
    }
}
