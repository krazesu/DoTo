package com.kraze.doto.auth;

import com.kraze.doto.common.exception.ConflictException;
import com.kraze.doto.security.JwtService;
import com.kraze.doto.user.User;
import com.kraze.doto.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String username = req.username();
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username already registered");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setDisplayName(req.displayName().trim());
        users.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByUsername(req.username())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Login failed"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwt.generateToken(u), u.getId(), u.getUsername(), u.getDisplayName());
    }

}
