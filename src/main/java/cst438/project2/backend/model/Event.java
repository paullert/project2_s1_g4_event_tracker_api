package cst438.project2.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_desc")
    private String eventDescription;

    @Column(name = "starts_on")
    private LocalDateTime startsOn;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "location_id")
    private Long locationId;

    public Event(){
    }

    public Event(Long id, String eventName, String eventDescription, LocalDateTime startsOn, Long creatorId, LocalDateTime updatedAt, Long locationId) {
        this.id = id;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.startsOn = startsOn;
        this.creatorId = creatorId;
        this.updatedAt = updatedAt;
        this.locationId = locationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LocalDateTime getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDateTime startsOn) {
        this.startsOn = startsOn;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}

