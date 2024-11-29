package org.myexample.spinningmotion.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.LoginUseCase;
import org.myexample.spinningmotion.configuration.security.jwt.JwtUtils;
import org.myexample.spinningmotion.domain.login.LoginRequest;
import org.myexample.spinningmotion.domain.login.LoginResponse;
import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/tokens")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthenticationController {
    private final LoginUseCase loginUseCase;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${jwt.cookie.name}")
    private String cookieName;

    @PostMapping
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        Optional<UserEntity> authenticatedUserOptional = loginUseCase.authUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (authenticatedUserOptional.isPresent()) {
            UserEntity authenticatedUser = authenticatedUserOptional.get();
            String token = jwtUtils.generateToken(authenticatedUser);

            // Create secure cookie
            Cookie cookie = new Cookie(cookieName, token);
            cookie.setHttpOnly(true); //javascript cant access cookie
            cookie.setSecure(true); // Enable in production
            cookie.setPath("/"); // the cookie path
            cookie.setMaxAge(86400); // 24 hours
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            // Return user info without the token
            return ResponseEntity.ok(LoginResponse.builder()
                    .userId(authenticatedUser.getId())
                    .email(authenticatedUser.getEmail())
                    .isAdmin(authenticatedUser.getIsAdmin())
                    .build());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
    @GetMapping("/validate")
    public ResponseEntity<LoginResponse> validateToken(HttpServletRequest request) {
        String token = jwtUtils.getJwtFromCookies(request);
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String email = jwtUtils.getEmailFromJwtToken(token);
            Optional<UserEntity> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(LoginResponse.builder()
                        .userId(user.get().getId())
                        .email(user.get().getEmail())
                        .isAdmin(user.get().getIsAdmin())
                        .build());
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}