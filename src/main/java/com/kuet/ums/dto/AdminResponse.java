package com.kuet.ums.dto;

import com.kuet.ums.model.Admin;

public record AdminResponse(Long id, String username, String fullName, String email) {

    public static AdminResponse from(Admin a) {
        return new AdminResponse(a.getId(), a.getUsername(), a.getFullName(), a.getEmail());
    }
}
