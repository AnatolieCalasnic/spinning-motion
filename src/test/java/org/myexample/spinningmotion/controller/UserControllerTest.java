package org.myexample.spinningmotion.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.EmailAlreadyExistsException;
import org.myexample.spinningmotion.business.exception.UserNotFoundException;
import org.myexample.spinningmotion.business.interfc.UserUseCase;
import org.myexample.spinningmotion.domain.user.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserUseCase userUseCase;

    @Autowired
    private ObjectMapper objectMapper;

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
    void newUser_Success() throws Exception {
        when(userUseCase.createUser(any(CreateUserRequest.class))).thenReturn(createUserResponse);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fname").value("Donny"))
                .andExpect(jsonPath("$.lname").value("Trotre"))
                .andExpect(jsonPath("$.email").value("donny@agymnasium.com"));

        verify(userUseCase, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    void newUser_EmailAlreadyExists() throws Exception {
        when(userUseCase.createUser(any(CreateUserRequest.class))).thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("It appears a user with this email already exists."));

        verify(userUseCase, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        List<GetUserResponse> users = Arrays.asList(getUserResponse);
        when(userUseCase.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fname").value("Donny"))
                .andExpect(jsonPath("$[0].lname").value("Trotre"))
                .andExpect(jsonPath("$[0].email").value("donny@agymnasium.com"));

        verify(userUseCase, times(1)).getAllUsers();
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userUseCase.getUser(any(GetUserRequest.class))).thenReturn(getUserResponse);

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fname").value("Donny"))
                .andExpect(jsonPath("$.lname").value("Trotre"))
                .andExpect(jsonPath("$.email").value("donny@agymnasium.com"));

        verify(userUseCase, times(1)).getUser(any(GetUserRequest.class));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userUseCase.getUser(any(GetUserRequest.class))).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userUseCase, times(1)).getUser(any(GetUserRequest.class));
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(updateUserResponse);

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fname").value("Donny"))
                .andExpect(jsonPath("$.lname").value("Tretre"))
                .andExpect(jsonPath("$.email").value("danny@agymnasium.com"));

        verify(userUseCase, times(1)).updateUser(any(UpdateUserRequest.class));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userUseCase).deleteUser(1L);

        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id 1 has been deleted"));

        verify(userUseCase, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userUseCase).deleteUser(1L);

        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userUseCase, times(1)).deleteUser(1L);
    }
}