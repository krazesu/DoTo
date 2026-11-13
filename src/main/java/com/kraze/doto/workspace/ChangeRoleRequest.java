package com.kraze.doto.workspace;

import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest (@NotNull WorkspaceRole role){
}
