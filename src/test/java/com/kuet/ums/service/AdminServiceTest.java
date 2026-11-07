package com.kuet.ums.service;

import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.model.Admin;
import com.kuet.ums.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** UNIT TEST 6: the last remaining admin can never be deleted. */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private UserRules rules;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void delete_refusesToRemoveTheLastAdmin() {
        AdminService service = new AdminService(adminRepository, rules, passwordEncoder);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(new Admin()));
        when(adminRepository.count()).thenReturn(1L);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("last remaining admin");
        verify(adminRepository, never()).delete(any());
    }
}
