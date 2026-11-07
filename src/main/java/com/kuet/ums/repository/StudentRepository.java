package com.kuet.ums.repository;

import com.kuet.ums.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentRoll(String studentRoll);

    boolean existsByStudentRollAndIdNot(String studentRoll, Long id);
}
