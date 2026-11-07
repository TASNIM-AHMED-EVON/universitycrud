package com.kuet.ums.config;

import com.kuet.ums.model.Admin;
import com.kuet.ums.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first admin on startup so there is someone who can log in and create other users. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String fullName;
    private final String email;

    public DataInitializer(AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username}") String username,
                           @Value("${app.admin.password}") String password,
                           @Value("${app.admin.full-name}") String fullName,
                           @Value("${app.admin.email}") String email) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            return;
        }
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFullName(fullName);
        admin.setEmail(email);
        adminRepository.save(admin);
        log.info("Default admin '{}' created. Change its password before real use.", username);
    }
}
