package cst438.project2.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "locations")
public class Location {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "location_id")
  private Long locationId;

  @Column(name = "location_name", length = 255)
  private String locationName;

  @Column(name = "address", nullable = false, length = 255)
  private String address;

  @Column(name = "location_desc")
  private String locationDescription;

  @JoinColumn(name = "creator_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private User creator;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Location(String name, String adr, String desc, User creator) {
    this.locationName = name;
    this.address = adr;
    this.locationDescription = desc;
    this.creator = creator;
  }

  public Location() {}

  public Long getLocationId() {
    return locationId;
  }

  public String getLocationName() {
    return locationName;
  }

  public String getAddress() {
    return address;
  }

  public String getLocationDescription() {
    return locationDescription;
  }

  public User getCreator() {
    return creator;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setLocationDescription(String locationDescription) {
    this.locationDescription = locationDescription;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}

