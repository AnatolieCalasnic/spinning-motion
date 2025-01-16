package org.myexample.spinningmotion.configuration.security;

import org.myexample.spinningmotion.configuration.security.auth.AuthenticationRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           AuthenticationEntryPoint authenticationEntryPoint,
                                           AuthenticationRequestFilter authenticationRequestFilter) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry ->
                        registry
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/tokens").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user").permitAll()
                                .requestMatchers("/genres/**").permitAll()
                                .requestMatchers("/purchase-history/**").permitAll()
                                .requestMatchers("/records/**").permitAll()
                                .requestMatchers("/search/**").permitAll()
                                .requestMatchers("/api/payment/**").permitAll()
                                .requestMatchers("/guest-orders/**").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/topic/**").permitAll()
                                .requestMatchers("/app/**").permitAll()
                                .requestMatchers("/subscriber/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/user/{id}").permitAll()
                                .requestMatchers(HttpMethod.POST, "/coupons/generate").permitAll()
                                .requestMatchers(HttpMethod.GET, "/coupons/user/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/coupons/{couponCode}/use").permitAll()
                                .requestMatchers(HttpMethod.GET, "/coupons/validate/**").permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(configure -> configure.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(authenticationRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With","Origin","Stripe-Signature"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true); // cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

