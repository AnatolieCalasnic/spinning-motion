package org.myexample.spinningmotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.EmailAlreadyExistsException;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.UserNotFoundException;
import org.myexample.spinningmotion.business.impl.notification.NotificationUseCaseImpl;
import org.myexample.spinningmotion.business.impl.user.UserTrackingUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.UserUseCase;
import org.myexample.spinningmotion.domain.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:3000/")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final NotificationUseCaseImpl notificationUseCase;
    private final UserTrackingUseCaseImpl userTrackingUseCaseImpl;

    @PostMapping("/user")
    public ResponseEntity<CreateUserResponse> newUser(@Valid @RequestBody CreateUserRequest request) {
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        CreateUserResponse response = userUseCase.createUser(request);
        notificationUseCase.sendAuthenticationNotification(
                "New user registered: " + response.getEmail(),
                "SUCCESS"
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<List<GetUserResponse>> getAllUsers() {
        List<GetUserResponse> users = userUseCase.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<GetUserResponse> getUserById(@PathVariable Long id) {
        GetUserResponse response = userUseCase.getUser(new GetUserRequest(id));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UpdateUserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request,
                                                         @PathVariable Long id) {
        request.setId(id);
        UpdateUserResponse response = userUseCase.updateUser(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.ok("User with id " + id + " has been deleted");
    }
    @GetMapping("/active-users")
    public ResponseEntity<Integer> getActiveUsersCount() {
        return ResponseEntity.ok(userTrackingUseCaseImpl.getActiveUsersCount());
    }
    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for User
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleUsernameAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<String> handleInvalidInput(InvalidInputException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
