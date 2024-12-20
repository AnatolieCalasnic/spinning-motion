package org.myexample.spinningmotion.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.notification.NotificationUseCaseImpl;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.EmailAlreadyExistsException;
import org.myexample.spinningmotion.business.exception.UserNotFoundException;
import org.myexample.spinningmotion.business.interfc.UserUseCase;
import org.myexample.spinningmotion.domain.user.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserUseCase userUseCase;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationUseCaseImpl notificationUseCase;
    @InjectMocks
    private UserController controller;

    private CreateUserRequest createUserRequest;
    private CreateUserResponse createUserResponse;
    private GetUserResponse getUserResponse;
    private UpdateUserRequest updateUserRequest;
    private UpdateUserResponse updateUserResponse;

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

        createUserResponse = CreateUserResponse.builder()
                .id(1L)
                .fname("Donny")
                .lname("Trotre")
                .email("donny@agymnasium.com")
                .build();

        getUserResponse = GetUserResponse.builder()
                .id(1L)
                .fname("Donny")
                .lname("Trotre")
                .email("donny@agymnasium.com")
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .id(1L)
                .fname("Donny")
                .lname("Tretre")
                .email("danny@agymnasium.com")
                .build();

        updateUserResponse = UpdateUserResponse.builder()
                .id(1L)
                .fname("Donny")
                .lname("Tretre")
                .email("danny@agymnasium.com")
                .build();
    }

    @Test
    void newUser_Success() {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userUseCase.createUser(any())).thenReturn(createUserResponse);
        doNothing().when(notificationUseCase).sendAuthenticationNotification(any(), any());

        ResponseEntity<CreateUserResponse> response = controller.newUser(createUserRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createUserResponse, response.getBody());
        verify(passwordEncoder).encode("sososo123123");
        verify(notificationUseCase).sendAuthenticationNotification(
                "New user registered: donny@agymnasium.com",
                "SUCCESS"
        );
    }

    @Test
    void newUser_EmailExists() {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userUseCase.createUser(any())).thenThrow(new EmailAlreadyExistsException());
        assertThrows(EmailAlreadyExistsException.class, () -> controller.newUser(createUserRequest));
        verify(passwordEncoder).encode("sososo123123");
    }

    @Test
    void getAllUsers_Success() {
        List<GetUserResponse> users = Arrays.asList(getUserResponse);
        when(userUseCase.getAllUsers()).thenReturn(users);
        ResponseEntity<List<GetUserResponse>> response = controller.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void getUserById_Success() {
        when(userUseCase.getUser(any())).thenReturn(getUserResponse);
        ResponseEntity<GetUserResponse> response = controller.getUserById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getUserResponse, response.getBody());
    }

    @Test
    void getUserById_NotFound() {
        when(userUseCase.getUser(any()))
                .thenThrow(new UserNotFoundException("User not found"));
        assertThrows(UserNotFoundException.class, () -> controller.getUserById(1L));
    }

    @Test
    void updateUser_Success() {
        when(userUseCase.updateUser(any())).thenReturn(updateUserResponse);
        ResponseEntity<UpdateUserResponse> response = controller.updateUser(updateUserRequest,1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updateUserResponse, response.getBody());
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userUseCase).deleteUser(1L);
        ResponseEntity<String> response = controller.deleteUser(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User with id 1 has been deleted", response.getBody());
    }

    @Test
    void deleteUser_NotFound() {
        doThrow(new UserNotFoundException("User not found"))
                .when(userUseCase).deleteUser(1L);
        assertThrows(UserNotFoundException.class, () -> controller.deleteUser(1L));
    }
}