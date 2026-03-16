package com.spendiq.dto.response;

import com.spendiq.entity.GroupSplit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupSplitResponse {
    private Long id;
    private String title;
    private BigDecimal totalAmount;
    private BigDecimal perHead;
    private LocalDate date;
    private String note;
    private UserResponse paidBy;
    private Set<UserResponse> members;
    private List<SplitOweResponse> owes;
    private String qrImageUrl;
    private int pendingCount;
    private LocalDateTime createdAt;

    private GroupSplitResponse() {}

    public static GroupSplitResponse from(GroupSplit g, String baseUrl) {
        GroupSplitResponse r = new GroupSplitResponse();
        r.id = g.getId(); r.title = g.getTitle();
        r.totalAmount = g.getTotalAmount(); r.perHead = g.getPerHead();
        r.date = g.getDate(); r.note = g.getNote();
        r.paidBy = UserResponse.from(g.getPaidBy());
        r.members = g.getMembers().stream().map(UserResponse::from).collect(Collectors.toSet());
        r.owes = g.getOwes().stream().map(SplitOweResponse::from).collect(Collectors.toList());
        r.pendingCount = (int) g.getOwes().stream().filter(o -> !o.isPaid()).count();
        r.qrImageUrl = g.getQrImagePath() != null ? baseUrl + "/api/splits/" + g.getId() + "/qr" : null;
        r.createdAt = g.getCreatedAt();
        return r;
    }

    public Long getId()                    { return id; }
    public String getTitle()               { return title; }
    public BigDecimal getTotalAmount()     { return totalAmount; }
    public BigDecimal getPerHead()         { return perHead; }
    public LocalDate getDate()             { return date; }
    public String getNote()               { return note; }
    public UserResponse getPaidBy()        { return paidBy; }
    public Set<UserResponse> getMembers()  { return members; }
    public List<SplitOweResponse> getOwes(){ return owes; }
    public String getQrImageUrl()          { return qrImageUrl; }
    public int getPendingCount()           { return pendingCount; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
}