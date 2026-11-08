package com.kraze.doto.security;

import com.kraze.doto.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("No Bearer token on {} {}", request.getMethod(), request.getRequestURI());
        } else {
            try {
                Long userId = jwtService.extractUserId(header.substring(7).trim());
                userRepository.findById(userId).ifPresentOrElse(user -> {
                    var principal = new AuthenticatedUser(user.getId(), user.getUsername());
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }, () -> log.warn("Token is valid but user {} does not exist", userId));
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("JWT rejected: {}", e.toString());
            }
        }
        chain.doFilter(request, response);
    }
}