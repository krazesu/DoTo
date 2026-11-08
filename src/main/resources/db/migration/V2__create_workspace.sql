CREATE TABLE workspace(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    invite_code     VARCHAR(12 NOT NULL UNIQUE,
    owner_id        BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE worksw(
    id              BIGSERIAL PRIMARY KEY,
    workspace_id    BIGINT  NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT      uq_workspace_member UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_workspace_members_user ON workspace_members(user_id);