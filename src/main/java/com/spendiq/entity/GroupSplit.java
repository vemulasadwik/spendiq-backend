package com.spendiq.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "group_splits")
public class GroupSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal perHead;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_user_id", nullable = false)
    private User paidBy;

    @ManyToMany
    @JoinTable(name = "group_split_members",
        joinColumns = @JoinColumn(name = "split_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "groupSplit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SplitOwe> owes = new ArrayList<>();

    @Column(name = "qr_image_path")
    private String qrImagePath;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public GroupSplit() {}

    private GroupSplit(Builder b) {
        this.title = b.title; this.totalAmount = b.totalAmount; this.perHead = b.perHead;
        this.date = b.date; this.note = b.note; this.paidBy = b.paidBy;
        this.members = b.members != null ? b.members : new HashSet<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title; private BigDecimal totalAmount, perHead;
        private LocalDate date; private String note; private User paidBy;
        private Set<User> members;
        public Builder title(String v)           { this.title = v;       return this; }
        public Builder totalAmount(BigDecimal v)  { this.totalAmount = v; return this; }
        public Builder perHead(BigDecimal v)      { this.perHead = v;     return this; }
        public Builder date(LocalDate v)          { this.date = v;        return this; }
        public Builder note(String v)             { this.note = v;        return this; }
        public Builder paidBy(User v)             { this.paidBy = v;      return this; }
        public Builder members(Set<User> v)       { this.members = v;     return this; }
        public GroupSplit build() { return new GroupSplit(this); }
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId()                { return id; }
    public String getTitle()           { return title; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPerHead()     { return perHead; }
    public LocalDate getDate()         { return date; }
    public String getNote()            { return note; }
    public User getPaidBy()            { return paidBy; }
    public Set<User> getMembers()      { return members; }
    public List<SplitOwe> getOwes()    { return owes; }
    public String getQrImagePath()     { return qrImagePath; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setQrImagePath(String v) { this.qrImagePath = v; }
    public void setMembers(Set<User> v)  { this.members = v; }
}