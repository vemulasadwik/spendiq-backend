package com.spendiq.dto.response;

import com.spendiq.entity.GroupSplit;
import com.spendiq.entity.Notification;
import com.spendiq.entity.SplitOwe;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private Long splitId;
    private String splitTitle;
    private String paidByName;
    private BigDecimal amount;
    private LocalDate splitDate;
    private boolean dismissed;
    private LocalDateTime createdAt;

    private NotificationResponse() {}

    public static NotificationResponse from(Notification n) {
        GroupSplit g = n.getGroupSplit();
        BigDecimal amount = g.getOwes().stream()
                .filter(o -> o.getUser().getId().equals(n.getUser().getId()))
                .map(SplitOwe::getAmount)
                .findFirst()
                .orElse(g.getPerHead());

        NotificationResponse r = new NotificationResponse();
        r.id = n.getId(); r.splitId = g.getId(); r.splitTitle = g.getTitle();
        r.paidByName = g.getPaidBy().getName(); r.amount = amount;
        r.splitDate = g.getDate(); r.dismissed = n.isDismissed();
        r.createdAt = n.getCreatedAt();
        return r;
    }

    public Long getId()           { return id; }
    public Long getSplitId()      { return splitId; }
    public String getSplitTitle() { return splitTitle; }
    public String getPaidByName() { return paidByName; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getSplitDate(){ return splitDate; }
    public boolean isDismissed()  { return dismissed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}