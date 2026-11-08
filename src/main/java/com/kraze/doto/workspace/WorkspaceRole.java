package com.kraze.doto.workspace;

public enum WorkspaceRole {
    MEMBER, ADMIN, OWNER;

    public boolean atLeast(WorkspaceRole required) {
        return this.ordinal() >= required.ordinal();
    }
}
