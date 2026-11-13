package com.kraze.doto.workspace;

import java.time.Instant;

public record WorkspaceResponse(
    Long id,
    String name,
    String description,
    String inviteCode,
    Long ownerId,
    Instant createdAt,
    WorkspaceRole myRole
){}

