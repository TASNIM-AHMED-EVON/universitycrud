package com.kuet.ums.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TEACHER")
public class Teacher extends AppUser {

    @Column(name = "teacher_department", length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    @Override
    public Role getRole() {
        return Role.TEACHER;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
