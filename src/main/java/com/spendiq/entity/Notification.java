package com.spendiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_id", nullable = false)
    private GroupSplit groupSplit;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean dismissed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    public Notification() {}

    private Notification(Builder b) {
        this.user = b.user; this.groupSplit = b.groupSplit; this.message = b.message;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User user; private GroupSplit groupSplit; private String message;
        public Builder user(User v)             { this.user = v;       return this; }
        public Builder groupSplit(GroupSplit v)  { this.groupSplit = v; return this; }
        public Builder message(String v)        { this.message = v;    return this; }
        public Notification build() { return new Notification(this); }
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public void dismiss() { this.dismissed = true; this.dismissedAt = LocalDateTime.now(); }

    public Long getId()              { return id; }
    public User getUser()            { return user; }
    public GroupSplit getGroupSplit() { return groupSplit; }
    public String getMessage()       { return message; }
    public boolean isDismissed()     { return dismissed; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getDismissedAt() { return dismissedAt; }
}