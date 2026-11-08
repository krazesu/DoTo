package com.kraze.doto.auth;

public record AuthResponse(String accessToken, Long userId, String username, String displayName) {}
