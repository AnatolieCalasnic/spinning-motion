package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    @Test
    void authUser_WithValidCredentials_ReturnsUser() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        UserEntity user = UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // Act
        Optional<UserEntity> result = loginUseCase.authUser(email, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    void authUser_WithInvalidPassword_ReturnsEmpty() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword123";

        UserEntity user = UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act
        Optional<UserEntity> result = loginUseCase.authUser(email, password);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    void authUser_WithNonexistentUser_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<UserEntity> result = loginUseCase.authUser(email, password);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
    }
}