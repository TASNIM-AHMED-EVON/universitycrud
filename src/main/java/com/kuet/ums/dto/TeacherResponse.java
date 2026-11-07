package com.kuet.ums.dto;

import com.kuet.ums.model.Teacher;

public record TeacherResponse(Long id, String username, String fullName, String email,
                              String department, String designation) {

    public static TeacherResponse from(Teacher t) {
        return new TeacherResponse(t.getId(), t.getUsername(), t.getFullName(), t.getEmail(),
                t.getDepartment(), t.getDesignation());
    }
}
