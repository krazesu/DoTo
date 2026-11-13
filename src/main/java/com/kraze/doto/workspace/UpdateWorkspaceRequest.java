package com.kraze.doto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWorkspaceRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
){}
