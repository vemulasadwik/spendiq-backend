package com.spendiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 5)
    private String avatar;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    public User() {}

    // Builder pattern
    private User(Builder b) {
        this.name = b.name;
        this.email = b.email;
        this.password = b.password;
        this.avatar = b.avatar;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name, email, password, avatar;
        public Builder name(String v)     { this.name = v;     return this; }
        public Builder email(String v)    { this.email = v;    return this; }
        public Builder password(String v) { this.password = v; return this; }
        public Builder avatar(String v)   { this.avatar = v;   return this; }
        public User build() { return new User(this); }
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId()                    { return id; }
    public String getName()                { return name; }
    public String getEmail()               { return email; }
    public String getPassword()            { return password; }
    public String getAvatar()              { return avatar; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public List<Expense> getExpenses()     { return expenses; }
    public List<Notification> getNotifications() { return notifications; }

    public void setId(Long id)             { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setEmail(String email)     { this.email = email; }
    public void setPassword(String pw)     { this.password = pw; }
    public void setAvatar(String avatar)   { this.avatar = avatar; }
}