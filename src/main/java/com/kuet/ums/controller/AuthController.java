package com.kuet.ums.controller;

import com.kuet.ums.dto.LoginRequest;
import com.kuet.ums.dto.UserInfoResponse;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.model.AppUser;
import com.kuet.ums.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/login   -> checks credentials, starts a session (JSESSIONID cookie)
 * POST /api/auth/logout  -> handled by Spring Security (see SecurityConfig)
 * GET  /api/auth/me      -> who am I?
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SecurityContextHolderStrategy contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public UserInfoResponse login(@Valid @RequestBody LoginRequest body,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // Throws BadCredentialsException (-> 401) if username/password are wrong
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));

        // Protect against session fixation: new session id after login
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext context = contextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return currentUser(authentication.getName());
    }

    @GetMapping("/me")
    public UserInfoResponse me(Authentication authentication) {
        return currentUser(authentication.getName());
    }

    private UserInfoResponse currentUser(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserInfoResponse.from(user);
    }
}
