package com.kuet.ums.dto;

import com.kuet.ums.model.AppUser;
import com.kuet.ums.model.Role;

public record UserInfoResponse(Long id, String username, String fullName, Role role) {

    public static UserInfoResponse from(AppUser u) {
        return new UserInfoResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole());
    }
}
