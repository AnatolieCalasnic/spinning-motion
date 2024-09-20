package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.InvalidEmailFormatException;
import org.springframework.boot.test.context.SpringBootTest;
import org.myexample.spinningmotion.business.exception.UserNotFoundException;
import org.myexample.spinningmotion.business.exception.EmailAlreadyExistsException;
import org.myexample.spinningmotion.domain.user.*;
import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.UserEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUseCaseImpl userUseCase;

    private CreateUserRequest createUserRequest;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
                .fname("Donny")
                .lname("Trotre")
                .email("donny@agymnasium.com")
                .password("sososo123123")
                .address("Dalgas Avenue")
                .postalCode("2000")
                .country("Denmark")
                .city("Aarhus")
                .region("Nord Brabrand")
                .phonenum("50123053")
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .fname("Donny")
                .lname("Trotre")
                .email("donny@agymnasium.com")
                .password("sososo123123")
                .address("Dalgas Avenue")
                .postalCode("2000")
                .country("Denmark")
                .city("Aarhus")
                .region("Nord Brabrand")
                .phonenum("50123053")
                .isAdmin(false)
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        CreateUserResponse response = userUseCase.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(createUserRequest.getFname(), response.getFname());
        assertEquals(createUserRequest.getEmail(), response.getEmail());

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userUseCase.createUser(createUserRequest));

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
    @Test
    void createUser_DuplicateEmail() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> userUseCase.createUser(createUserRequest));
    }
    @Test
    void createUser_InvalidEmailFormat() {
        createUserRequest.setEmail("invalid-email");
        assertThrows(InvalidEmailFormatException.class, () -> userUseCase.createUser(createUserRequest));
    }
    @Test
    void getUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        GetUserResponse response = userUseCase.getUser(new GetUserRequest(1L));

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(userEntity.getFname(), response.getFname());
        assertEquals(userEntity.getEmail(), response.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userUseCase.getUser(new GetUserRequest(1L)));

        verify(userRepository).findById(1L);
    }

    @Test
    void getAllUsers_Success() {
        List<UserEntity> userEntities = Arrays.asList(userEntity);
        when(userRepository.findAll()).thenReturn(userEntities);

        List<GetUserResponse> responses = userUseCase.getAllUsers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(userEntity.getId(), responses.get(0).getId());
        assertEquals(userEntity.getFname(), responses.get(0).getFname());

        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .id(1L)
                .fname("Jane")
                .lname("Doe")
                .email("jane.doe@example.com")
                .address("456 Elm St")
                .postalCode("67890")
                .country("Canada")
                .city("Toronto")
                .region("ON")
                .phonenum("0987654321")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UpdateUserResponse response = userUseCase.updateUser(updateRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(updateRequest.getFname(), response.getFname());
        assertEquals(updateRequest.getEmail(), response.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void updateUser_UserNotFound() {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userUseCase.updateUser(updateRequest));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        assertDoesNotThrow(() -> userUseCase.deleteUser(1L));

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userUseCase.deleteUser(1L));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

}