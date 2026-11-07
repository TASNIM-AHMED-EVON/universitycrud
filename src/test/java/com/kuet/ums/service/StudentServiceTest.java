package com.kuet.ums.service;

import com.kuet.ums.dto.StudentRequest;
import com.kuet.ums.dto.StudentResponse;
import com.kuet.ums.exception.DuplicateResourceException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.model.Student;
import com.kuet.ums.repository.StudentRepository;
import com.kuet.ums.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** UNIT TESTS 3-5: StudentService with mocked repositories and a mocked password encoder. */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private StudentService service;

    @BeforeEach
    void setUp() {
        // Real UserRules on top of a mocked UserRepository (all existsBy... return false by default)
        service = new StudentService(studentRepository, new UserRules(userRepository), passwordEncoder);
    }

    private StudentRequest request() {
        return new StudentRequest("rafi", "secret123", "Rafi Ahmed", "rafi@kuet.ac.bd",
                "2207001", "CSE", 6);
    }

    @Test
    void create_hashesPasswordAndSavesStudent() {
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse response = service.create(request());

        ArgumentCaptor<Student> saved = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("HASHED"); // raw password never stored
        assertThat(response.username()).isEqualTo("rafi");
        assertThat(response.studentRoll()).isEqualTo("2207001");
    }

    @Test
    void create_throwsWhenRollAlreadyRegistered() {
        when(studentRepository.existsByStudentRoll("2207001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("2207001");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void findById_throwsWhenStudentDoesNotExist() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
