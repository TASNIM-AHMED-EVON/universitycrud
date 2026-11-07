package com.kuet.ums.service;

import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.DuplicateResourceException;
import com.kuet.ums.repository.UserRepository;
import org.springframework.stereotype.Component;

/** Rules shared by the Admin, Teacher and Student services. */
@Component
public class UserRules {

    private final UserRepository userRepository;

    public UserRules(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Username and email must be unique across ALL roles. Pass null as excludeId when creating. */
    public void checkUnique(Long excludeId, String username, String email) {
        boolean usernameTaken = excludeId == null
                ? userRepository.existsByUsername(username)
                : userRepository.existsByUsernameAndIdNot(username, excludeId);
        if (usernameTaken){
            throw new DuplicateResourceException("Username '" + username + "' is already taken");
        }
        boolean emailTaken = excludeId == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, excludeId);
        if (emailTaken) {
            throw new DuplicateResourceException("Email '" + email + "' is already registered");
        }
    }

    public String requirePassword(String rawPassword) {
        if (rawPassword == null) {
            throw new BadRequestException("Password is required when creating a user");
        }
        return rawPassword;
    }

    public boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
