package com.spendiq.dto.response;

import com.spendiq.entity.SplitOwe;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SplitOweResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private BigDecimal amount;
    private boolean paid;
    private LocalDateTime paidAt;

    private SplitOweResponse() {}

    public static SplitOweResponse from(SplitOwe o) {
        SplitOweResponse r = new SplitOweResponse();
        r.id = o.getId(); r.userId = o.getUser().getId();
        r.userName = o.getUser().getName(); r.userAvatar = o.getUser().getAvatar();
        r.amount = o.getAmount(); r.paid = o.isPaid(); r.paidAt = o.getPaidAt();
        return r;
    }

    public Long getId()           { return id; }
    public Long getUserId()       { return userId; }
    public String getUserName()   { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public BigDecimal getAmount() { return amount; }
    public boolean isPaid()       { return paid; }
    public LocalDateTime getPaidAt() { return paidAt; }
}