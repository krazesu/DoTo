package com.kraze.doto.user;

import com.kraze.doto.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository users;

    public record UserResponse(Long id, String username, String displayName) {}

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        User u = users.findById(principal.id()).orElseThrow();
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName());
    }
}
