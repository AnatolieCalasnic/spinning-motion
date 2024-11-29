package org.myexample.spinningmotion.configuration.security.auth;

import org.myexample.spinningmotion.configuration.security.token.AccessToken;
import org.myexample.spinningmotion.configuration.security.token.AccessTokenDecoder;
import org.myexample.spinningmotion.configuration.security.token.exception.InvalidAccessTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthenticationRequestFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationRequestFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenDecoder accessTokenDecoder;

    @Value("${jwt.cookie.name}")
    private String cookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                AccessToken accessToken = accessTokenDecoder.decode(token);
                SecurityContextHolder.getContext().setAuthentication(
                        convertToSpringToken(accessToken)
                );
            }
        } catch (InvalidAccessTokenException e) {
            LOG.error("Error validating access token: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String tokenFromCookie = extractTokenFromCookie(request);
        if (tokenFromCookie != null) {
            return tokenFromCookie;
        }
        return extractTokenFromHeader(request);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(cookieName))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private UsernamePasswordAuthenticationToken convertToSpringToken(AccessToken accessToken) {
        List<SimpleGrantedAuthority> authorities = Boolean.TRUE.equals(accessToken.getIsAdmin())
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        UserDetails userDetails = new User(
                accessToken.getSubject(),
                "",
                authorities
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

}