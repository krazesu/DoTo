CREATE TABLE tasks (
    id                  BIGSERIAL PRIMARY KEY,
    workspace_id        BIGINT  NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    title               VARCHAR(200) NOT NULL,
    description         VARCHAR(2000),
    status              VARCHAR(20) NOT NULL,
    priority            VARCHAR(20) NOT NULL,
    due_date            DATE,
    assignee_id         BIGINT  REFERENCES users(id) ON DELETE SET NULL,
    created_by          BIGINT  NOT NULL REFERENCES users(id),
    version             BIGINT  NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_workspace_status ON tasks(workspace_id, status);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);

CREATE TABLE subtasks (
    id                  BIGSERIAL PRIMARY KEY,
    task_id             BIGINT  NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title               VARCHAR(200) NOT NULL,
    done                BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_subtasks_task ON subtasks(task_id);

CREATE TABLE activity_logs (
    id                  BIGSERIAL PRIMARY KEY,
    workspace_id        BIGINT  NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    actor_id            BIGINT  NOT NULL REFERENCES users(id),
    action              VARCHAR(40) NOT NULL,
    target_type         VARCHAR(20) NOT NULL,
    target_id           BIGINT      NOT NULL,
    details             VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_workspace_created ON activity_logs(workspace_id, created_at DESC);