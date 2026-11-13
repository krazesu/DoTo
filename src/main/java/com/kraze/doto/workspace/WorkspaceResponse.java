package com.kraze.doto.workspace;

import java.time.Instant;

public record WorkspaceResponse(
    Long id,
    String name,
    String description,
    String inviteCode, //visible only to OWNER or ADMIN
    Long ownerId,
    Instant createdAt,
    WorkspaceRole myRole
){}

