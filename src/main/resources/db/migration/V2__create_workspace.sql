CREATE TABLE workspace(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    invite_code     VARCHAR(12 NOT NULL UNIQUE,
    owner_id        BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE(
    id              BIGSERIAL PRIMARY KEY,
    workspace_id    BIGINT  NOT NULL REFERENCES,
)