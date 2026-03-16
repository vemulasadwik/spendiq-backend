package com.spendiq.dto.response;

import com.spendiq.entity.User;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String avatar;

    private UserResponse() {}

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId(); r.name = u.getName();
        r.email = u.getEmail(); r.avatar = u.getAvatar();
        return r;
    }

    public Long getId()     { return id; }
    public String getName() { return name; }
    public String getEmail(){ return email; }
    public String getAvatar(){ return avatar; }
}