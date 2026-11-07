package com.kuet.ums.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends AppUser {

    @Column(name = "student_roll", unique = true, length = 30)
    private String studentRoll;

    @Column(name = "student_department", length = 100)
    private String department;

    private Integer semester;

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    public String getStudentRoll() { return studentRoll; }
    public void setStudentRoll(String studentRoll) { this.studentRoll = studentRoll; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
}
