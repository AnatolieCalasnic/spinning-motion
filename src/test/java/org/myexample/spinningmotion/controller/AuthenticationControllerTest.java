package org.myexample.spinningmotion.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.myexample.spinningmotion.business.impl.notification.NotificationUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.LoginUseCase;
import org.myexample.spinningmotion.configuration.security.jwt.JwtUtils;
import org.myexample.spinningmotion.domain.login.LoginRequest;
import org.myexample.spinningmotion.domain.login.LoginResponse;
import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationUseCaseImpl notificationUseCase;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthenticationController authController;

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    private LoginRequest loginRequest;
    private UserEntity userEntity;
    private static final String TEST_JWT = "test.jwt.token";
    private static final String TEST_COOKIE_NAME = "test_cookie";

    @BeforeEach
    void setUp() {
        // Set cookie name using reflection since @Value can't be easily mocked
        ReflectionTestUtils.setField(authController, "cookieName", TEST_COOKIE_NAME);

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword")
                .fname("Test User")
                .isAdmin(false)
                .build();
    }

    @Test
    void login_ValidCredentials_ReturnsLoginResponseAndSetsCookie() {
        // Arrange
        when(loginUseCase.authUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenReturn(Optional.of(userEntity));
        when(jwtUtils.generateToken(userEntity)).thenReturn(TEST_JWT);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertFalse(response.getBody().getIsAdmin());

        // Verify cookie settings
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(TEST_COOKIE_NAME, cookie.getName());
        assertEquals(TEST_JWT, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(86400, cookie.getMaxAge());
        assertEquals("Lax", cookie.getAttribute("SameSite"));

        verify(notificationUseCase).sendAuthenticationNotification(
                "User logged in: test@example.com",
                "SUCCESS"
        );
    }

    @Test
    void login_InvalidCredentials_ReturnsBadRequest() {
        // Arrange
        when(loginUseCase.authUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify no cookie was set
        verify(httpServletResponse, never()).addCookie(any(Cookie.class));
        verify(jwtUtils, never()).generateToken(any());
        verify(notificationUseCase, never()).sendAuthenticationNotification(any(), any());
    }
    @Test
    void logout_WithValidToken_ClearsTokenCookieAndSendsNotification() {
        // Arrange
        String validToken = "valid.jwt.token";
        when(jwtUtils.getJwtFromCookies(httpServletRequest)).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getEmailFromJwtToken(validToken)).thenReturn("test@example.com");

        // Act
        ResponseEntity<Void> response = authController.logout(httpServletResponse, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cookie settings
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(TEST_COOKIE_NAME, cookie.getName());
        assertEquals("", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge());
        assertEquals("Lax", cookie.getAttribute("SameSite"));

        // Verify notification was sent
        verify(notificationUseCase).sendAuthenticationNotification(
                "User logged out: test@example.com",
                "INFO"
        );
    }

    @Test
    void logout_WithInvalidToken_ClearsTokenCookieWithoutNotification() {
        // Arrange
        when(jwtUtils.getJwtFromCookies(httpServletRequest)).thenReturn("invalid.token");
        when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);

        // Act
        ResponseEntity<Void> response = authController.logout(httpServletResponse, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cookie settings
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(TEST_COOKIE_NAME, cookie.getName());
        assertEquals("", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge());
        assertEquals("Lax", cookie.getAttribute("SameSite"));

        // Verify no notification was sent
        verify(notificationUseCase, never()).sendAuthenticationNotification(any(), any());
    }
    @Test
    void logout_ClearsTokenCookie() {
        // Act
        ResponseEntity<Void> response = authController.logout(httpServletResponse, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cookie settings
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(TEST_COOKIE_NAME, cookie.getName());
        assertEquals("", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge());
        assertEquals("Lax", cookie.getAttribute("SameSite"));
    }

    @Test
    void login_AdminUser_ReturnsLoginResponseWithAdminFlag() {
        // Arrange
        UserEntity adminUser = UserEntity.builder()
                .id(1L)
                .email("admin@example.com")
                .password("hashedPassword")
                .fname("Admin User")
                .isAdmin(true)
                .build();

        LoginRequest adminLoginRequest = LoginRequest.builder()
                .email("admin@example.com")
                .password("password123")
                .build();

        when(loginUseCase.authUser(adminLoginRequest.getEmail(), adminLoginRequest.getPassword()))
                .thenReturn(Optional.of(adminUser));
        when(jwtUtils.generateToken(adminUser)).thenReturn(TEST_JWT);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(adminLoginRequest, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("admin@example.com", response.getBody().getEmail());
        assertTrue(response.getBody().getIsAdmin());

        // Verify cookie was set
        verify(httpServletResponse).addCookie(any(Cookie.class));
        verify(notificationUseCase).sendAuthenticationNotification(
                "User logged in: admin@example.com",
                "SUCCESS"
        );
    }

    @Test
    void login_NullCredentials_ReturnsBadRequest() {
        LoginRequest nullRequest = LoginRequest.builder()
                .email(null)
                .password(null)
                .build();

        when(loginUseCase.authUser(null, null))
                .thenReturn(Optional.empty());
        ResponseEntity<LoginResponse> response = authController.login(nullRequest, httpServletResponse);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify no cookie was set
        verify(httpServletResponse, never()).addCookie(any(Cookie.class));
        verify(jwtUtils, never()).generateToken(any());
        verify(notificationUseCase, never()).sendAuthenticationNotification(any(), any());

    }
    @Test
    void validateToken_ValidToken_ReturnsLoginResponse() {
        String validToken = "valid.jwt.token";
        when(jwtUtils.getJwtFromCookies(httpServletRequest)).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getEmailFromJwtToken(validToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        ResponseEntity<LoginResponse> response = authController.validateToken(httpServletRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userEntity.getId(), response.getBody().getUserId());
        assertEquals(userEntity.getEmail(), response.getBody().getEmail());
        assertEquals(userEntity.getIsAdmin(), response.getBody().getIsAdmin());
    }

    @Test
    void validateToken_InvalidToken_ReturnsUnauthorized() {
        when(jwtUtils.getJwtFromCookies(httpServletRequest)).thenReturn("invalid.token");
        when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);
        ResponseEntity<LoginResponse> response = authController.validateToken(httpServletRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void validateToken_UserNotFound_ReturnsUnauthorized() {
        String validToken = "valid.jwt.token";
        when(jwtUtils.getJwtFromCookies(httpServletRequest)).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getEmailFromJwtToken(validToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        ResponseEntity<LoginResponse> response = authController.validateToken(httpServletRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void validateToken_NullToken_ReturnsUnauthorized() {
        when(jwtUtils.getJwtFromCookies(httpServletRequest)).thenReturn(null);
        ResponseEntity<LoginResponse> response = authController.validateToken(httpServletRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }
}