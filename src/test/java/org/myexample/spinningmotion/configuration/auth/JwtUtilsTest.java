package org.myexample.spinningmotion.configuration.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.configuration.security.jwt.JwtUtils;
import org.myexample.spinningmotion.persistence.entity.UserEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String jwtSecret = "testSecretKeyThatIsLongEnoughForHS256Algorithm";
    private final long jwtExpirationMs = 3600000; // 1 hour
    private final String jwtCookie = "testJwtCookie";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(jwtSecret, jwtExpirationMs, jwtCookie);
    }

    @Test
    void generateToken_ValidUser_ReturnsValidToken() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .isAdmin(true)
                .build();

        // Act
        String token = jwtUtils.generateToken(user);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("test@example.com", jwtUtils.getEmailFromJwtToken(token));
    }

    @Test
    void validateJwtToken_ValidToken_ReturnsTrue() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        String token = jwtUtils.generateToken(user);

        // Act & Assert
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_InvalidToken_ReturnsFalse() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken("invalid.token.here"));
    }

    @Test
    void getJwtFromCookies_ValidCookie_ReturnsToken() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = new Cookie[]{new Cookie(jwtCookie, "testToken")};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String token = jwtUtils.getJwtFromCookies(request);

        // Assert
        assertEquals("testToken", token);
    }

    @Test
    void getJwtFromCookies_NoCookies_ReturnsNull() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        // Act
        String token = jwtUtils.getJwtFromCookies(request);

        // Assert
        assertNull(token);
    }
}