package com.kuet.ums.service;

import com.kuet.ums.dto.StudentRequest;
import com.kuet.ums.dto.StudentResponse;
import com.kuet.ums.exception.DuplicateResourceException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.model.Student;
import com.kuet.ums.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRules rules;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, UserRules rules, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.rules = rules;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(StudentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentResponse.from(getOrThrow(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest r) {
        rules.checkUnique(null, r.username(), r.email());
        checkRollUnique(null, r.studentRoll());
        Student s = new Student();
        s.setPassword(passwordEncoder.encode(rules.requirePassword(r.password())));
        apply(s, r);
        return StudentResponse.from(studentRepository.save(s));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest r) {
        Student s = getOrThrow(id);
        rules.checkUnique(id, r.username(), r.email());
        checkRollUnique(id, r.studentRoll());
        if (rules.hasText(r.password())) {
            s.setPassword(passwordEncoder.encode(r.password()));
        }
        apply(s, r);
        return StudentResponse.from(studentRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        studentRepository.delete(getOrThrow(id));
    }

    private void checkRollUnique(Long excludeId, String roll) {
        boolean taken = excludeId == null
                ? studentRepository.existsByStudentRoll(roll)
                : studentRepository.existsByStudentRollAndIdNot(roll, excludeId);
        if (taken) {
            throw new DuplicateResourceException("Student roll '" + roll + "' is already registered");
        }
    }

    private void apply(Student s, StudentRequest r) {
        s.setUsername(r.username());
        s.setFullName(r.fullName());
        s.setEmail(r.email());
        s.setStudentRoll(r.studentRoll());
        s.setDepartment(r.department());
        s.setSemester(r.semester());
    }

    private Student getOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
    }
}
