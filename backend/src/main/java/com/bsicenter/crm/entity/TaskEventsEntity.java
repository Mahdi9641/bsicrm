package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for task_events. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "task_events")
public class TaskEventsEntity {
  public TaskEventsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "task_id", nullable = false)
  private Long taskId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "kind", nullable = false, length = 20)
  private String kind;

  @Column(name = "body", nullable = false, length = 2000)
  private String body;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private FollowUpTasksEntity taskReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity userReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long value) {
    this.taskId = value;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long value) {
    this.userId = value;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String value) {
    this.kind = value;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    this.body = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public FollowUpTasksEntity getTaskReference() {
    return taskReference;
  }

  public AppUsersEntity getUserReference() {
    return userReference;
  }
}
