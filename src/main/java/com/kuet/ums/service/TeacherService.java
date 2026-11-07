package com.kuet.ums.service;

import com.kuet.ums.dto.TeacherRequest;
import com.kuet.ums.dto.TeacherResponse;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.model.Teacher;
import com.kuet.ums.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRules rules;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository, UserRules rules, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.rules = rules;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll().stream().map(TeacherResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return TeacherResponse.from(getOrThrow(id));
    }

    @Transactional
    public TeacherResponse create(TeacherRequest r) {
        rules.checkUnique(null, r.username(), r.email());
        Teacher t = new Teacher();
        t.setPassword(passwordEncoder.encode(rules.requirePassword(r.password())));
        apply(t, r);
        return TeacherResponse.from(teacherRepository.save(t));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest r) {
        Teacher t = getOrThrow(id);
        rules.checkUnique(id, r.username(), r.email());
        if (rules.hasText(r.password())) {
            t.setPassword(passwordEncoder.encode(r.password()));
        }
        apply(t, r);
        return TeacherResponse.from(teacherRepository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        teacherRepository.delete(getOrThrow(id));
    }

    private void apply(Teacher t, TeacherRequest r) {
        t.setUsername(r.username());
        t.setFullName(r.fullName());
        t.setEmail(r.email());
        t.setDepartment(r.department());
        t.setDesignation(r.designation());
    }

    private Teacher getOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher with id " + id + " not found"));
    }
}
