package org.myexample.spinningmotion.configuration.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMillis;

    public String generateToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getEmail());
        claims.put("created", new Date());
        claims.put("userId", user.getId());
        claims.put("isAdmin", user.getIsAdmin());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMillis))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
