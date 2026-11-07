package com.kuet.ums.service;

import com.kuet.ums.dto.AdminRequest;
import com.kuet.ums.dto.AdminResponse;
import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.model.Admin;
import com.kuet.ums.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRules rules;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, UserRules rules, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.rules = rules;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> findAll() {
        return adminRepository.findAll().stream().map(AdminResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse findById(Long id) {
        return AdminResponse.from(getOrThrow(id));
    }

    @Transactional
    public AdminResponse create(AdminRequest r) {
        rules.checkUnique(null, r.username(), r.email());
        Admin a = new Admin();
        a.setUsername(r.username());
        a.setFullName(r.fullName());
        a.setEmail(r.email());
        a.setPassword(passwordEncoder.encode(rules.requirePassword(r.password())));
        return AdminResponse.from(adminRepository.save(a));
    }

    @Transactional
    public AdminResponse update(Long id, AdminRequest r) {
        Admin a = getOrThrow(id);
        rules.checkUnique(id, r.username(), r.email());
        a.setUsername(r.username());
        a.setFullName(r.fullName());
        a.setEmail(r.email());
        if (rules.hasText(r.password())) {
            a.setPassword(passwordEncoder.encode(r.password()));
        }
        return AdminResponse.from(adminRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        Admin a = getOrThrow(id);
        if (adminRepository.count() <= 1) {
            throw new BadRequestException("Cannot delete the last remaining admin");
        }
        adminRepository.delete(a);
    }

    private Admin getOrThrow(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin with id " + id + " not found"));
    }
}
