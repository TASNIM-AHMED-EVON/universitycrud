package com.kuet.ums.service;

import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.DuplicateResourceException;
import com.kuet.ums.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** UNIT TESTS 1-2: UserRules, with the repository mocked (no Spring, no database). */
@ExtendWith(MockitoExtension.class)
class UserRulesTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRules rules;

    @Test
    void checkUnique_throwsWhenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("rafi")).thenReturn(true);

        assertThatThrownBy(() -> rules.checkUnique(null, "rafi", "rafi@kuet.ac.bd"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("rafi");
    }

    @Test
    void requirePassword_rejectsBlankAndAcceptsRealPassword() {
        assertThatThrownBy(() -> rules.requirePassword("   "))
                .isInstanceOf(BadRequestException.class);

        assertThat(rules.requirePassword("secret123")).isEqualTo("secret123");
    }
}
