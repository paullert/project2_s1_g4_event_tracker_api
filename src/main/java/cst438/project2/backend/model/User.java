package cst438.project2.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "username", nullable = false, unique = true, length = 255)
  private String username;

  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  public String getEmail() {
    return email;
  }

  @Column(name = "google_subject", unique = true, nullable = false, length = 255)
  private String googleSubject;

  // Enums need special annotations
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "role", nullable = false, columnDefinition = "user_role") // columnDefinition passes raw SQL to the database, in this case the enum type
  private UserRole role = UserRole.USER; //    ... = ... means that it has a default value if none is specified

  @CreationTimestamp // Defaults value to current timestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp // On update, sets value to current timestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public User() {
  }

  public User(Long userId, String username, String googleSubject, UserRole role,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.userId = userId;
    this.username = username;
    this.googleSubject = googleSubject;
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getGoogleSubject() {
    return googleSubject;
  }

  public void setGoogleSubject(String googleSubject) {
    this.googleSubject = googleSubject;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}