package com.kuet.ums.dto;

import com.kuet.ums.model.Student;

public record StudentResponse(Long id, String username, String fullName, String email,
                              String studentRoll, String department, Integer semester) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getUsername(), s.getFullName(), s.getEmail(),
                s.getStudentRoll(), s.getDepartment(), s.getSemester());
    }
}
