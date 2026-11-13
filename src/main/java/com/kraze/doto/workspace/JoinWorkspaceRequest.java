package com.kraze.doto.workspace;

import jakarta.validation.constraints.NotBlank;

public record JoinWorkspaceRequest(@NotBlank String inviteCode){
}
