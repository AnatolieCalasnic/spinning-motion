package org.myexample.spinningmotion.configuration.security.token.impl;

import io.jsonwebtoken.security.Keys;
import org.myexample.spinningmotion.configuration.security.token.AccessToken;
import org.myexample.spinningmotion.configuration.security.token.AccessTokenDecoder;
import org.myexample.spinningmotion.configuration.security.token.AccessTokenEncoder;
import org.myexample.spinningmotion.configuration.security.token.exception.InvalidAccessTokenException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class AccessTokenEncoderDecoderImpl implements AccessTokenEncoder, AccessTokenDecoder {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenEncoderDecoderImpl.class);
    private static final String USER_ID_CLAIM = "userId";
    private static final String IS_ADMIN_CLAIM = "isAdmin";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey key;
    private final long jwtExpirationMillis;

    public AccessTokenEncoderDecoderImpl(@Value("${jwt.secret}") String secretKey,
                                         @Value("${jwt.expiration}") long jwtExpirationMillis) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key cannot be null or empty");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMillis = jwtExpirationMillis;
    }
    @Override
    public String encode(AccessToken accessToken) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMillis);

            Map<String, Object> claims = new HashMap<>();
            claims.put(USER_ID_CLAIM, accessToken.getUserId());
            claims.put(IS_ADMIN_CLAIM, accessToken.getIsAdmin());

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(accessToken.getSubject())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            logger.debug("Token encoded successfully for user: {}", accessToken.getSubject());
            return token;
        } catch (Exception e) {
            logger.error("Failed to encode access token: {}", e.getMessage());
            throw new InvalidAccessTokenException(e.getMessage());
        }
    }

    @Override
    public AccessToken decode(String accessTokenEncoded) {
        try {
            logger.debug("Attempting to decode token");

            String tokenValue = removeBearedPrefix(accessTokenEncoded);
            Claims claims = parseToken(tokenValue);

            return createAccessToken(claims);
        } catch (Exception e) {
            logger.error("Failed to decode access token: {}", e.getMessage());
            throw new InvalidAccessTokenException(e.getMessage());
        }
    }

    private String removeBearedPrefix(String token) {
        return token.startsWith(BEARER_PREFIX) ?
                token.substring(BEARER_PREFIX.length()) :
                token;
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private AccessToken createAccessToken(Claims claims) {
        String subject = claims.getSubject();
        Long userId = claims.get(USER_ID_CLAIM, Long.class);
        Boolean isAdmin = claims.get(IS_ADMIN_CLAIM, Boolean.class);

        logger.debug("Token decoded successfully for user: {}", subject);
        return new AccessTokenImpl(subject, userId, isAdmin);
    }
}