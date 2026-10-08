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
    private Long id;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_description")
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

    public Long getId(){
        return id;
    }

    public String getEventName(){
        return eventName;
    }

    public String getEventDescription(){
        return eventDescription;
    }

    public LocalDateTime getStartsOn(){
        return startsOn;
    }

    public Long getCreatorId(){
        return creatorId;
    }

    public Long getLocationId(){
        return locationId;
    }
}

