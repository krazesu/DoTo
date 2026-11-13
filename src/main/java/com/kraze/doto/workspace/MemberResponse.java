package com.kraze.doto.workspace;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String username,
        String displayName,
        WorkspaceRole role,
        Instant joinedAt
){}

