package org.myexample.spinningmotion.configuration.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.configuration.security.token.AccessToken;
import org.myexample.spinningmotion.configuration.security.token.exception.InvalidAccessTokenException;
import org.myexample.spinningmotion.configuration.security.token.impl.AccessTokenEncoderDecoderImpl;
import org.myexample.spinningmotion.configuration.security.token.impl.AccessTokenImpl;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenEncoderDecoderImplTest {

    private AccessTokenEncoderDecoderImpl tokenEncoderDecoder;
    private static final String SECRET_KEY = "thisIsAVeryLongSecretKeyForTestingPurposesOnly12345";
    private static final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenEncoderDecoder = new AccessTokenEncoderDecoderImpl(SECRET_KEY, EXPIRATION);
    }

    @Test
    void constructor_NullSecretKey_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new AccessTokenEncoderDecoderImpl(null, EXPIRATION));
    }

    @Test
    void constructor_EmptySecretKey_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new AccessTokenEncoderDecoderImpl("", EXPIRATION));
    }

    @Test
    void encode_ValidAccessToken_ReturnsEncodedToken() {
        // Arrange
        AccessToken accessToken = new AccessTokenImpl("test@example.com", 1L, false);

        // Act
        String encodedToken = tokenEncoderDecoder.encode(accessToken);

        // Assert
        assertNotNull(encodedToken);
        assertTrue(encodedToken.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void decode_ValidToken_ReturnsAccessToken() {
        // Arrange
        AccessToken originalToken = new AccessTokenImpl("test@example.com", 1L, false);
        String encodedToken = tokenEncoderDecoder.encode(originalToken);

        // Act
        AccessToken decodedToken = tokenEncoderDecoder.decode(encodedToken);

        // Assert
        assertNotNull(decodedToken);
        assertEquals(originalToken.getSubject(), decodedToken.getSubject());
        assertEquals(originalToken.getUserId(), decodedToken.getUserId());
        assertEquals(originalToken.getIsAdmin(), decodedToken.getIsAdmin());
    }

    @Test
    void decode_TokenWithBearerPrefix_ReturnsAccessToken() {
        // Arrange
        AccessToken originalToken = new AccessTokenImpl("test@example.com", 1L, false);
        String encodedToken = tokenEncoderDecoder.encode(originalToken);
        String tokenWithBearer = "Bearer " + encodedToken;

        // Act
        AccessToken decodedToken = tokenEncoderDecoder.decode(tokenWithBearer);

        // Assert
        assertNotNull(decodedToken);
        assertEquals(originalToken.getSubject(), decodedToken.getSubject());
        assertEquals(originalToken.getUserId(), decodedToken.getUserId());
        assertEquals(originalToken.getIsAdmin(), decodedToken.getIsAdmin());
    }

    @Test
    void decode_InvalidToken_ThrowsInvalidAccessTokenException() {
        assertThrows(InvalidAccessTokenException.class,
                () -> tokenEncoderDecoder.decode("invalid.token.string"));
    }

    @Test
    void encode_AndDecode_AdminUser_PreservesAdminStatus() {
        // Arrange
        AccessToken originalToken = new AccessTokenImpl("admin@example.com", 1L, true);

        // Act
        String encodedToken = tokenEncoderDecoder.encode(originalToken);
        AccessToken decodedToken = tokenEncoderDecoder.decode(encodedToken);

        // Assert
        assertTrue(decodedToken.getIsAdmin());
        assertEquals(originalToken.getSubject(), decodedToken.getSubject());
        assertEquals(originalToken.getUserId(), decodedToken.getUserId());
    }
}
