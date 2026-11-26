package com.kraze.doto.task;

import com.kraze.doto.common.dto.UserSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class TaskDtos {
    private TaskDtos() {}

    public record CreateTaskRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            TaskPriority priority,
            LocalDate dueDate,
            Long assigneeId
    ) {}

    public record UpdateTaskRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            @NotNull TaskPriority priority,
            LocalDate dueDate,
            @NotNull Long version
    ) {}

    public record UpdateStatusRequest(@NotNull TaskStatus status) {}

    public record AssignRequest(Long assigneeId) {}   // null = unassign

    public record CreateSubtaskRequest(@NotBlank @Size(max = 200) String title) {}

    public record UpdateSubtaskRequest(@Size(max = 200) String title, Boolean
    done) {}

    public record SubtaskResponse(Long id, Long taskId, String title, boolean done) {}

    public record TaskResponse(
            Long id,
            Long workspaceId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            boolean overdue,
            UserSummary assignee,
            UserSummary createdBy,
            List<SubtaskResponse> subtasks,
            int progress,
            Long version,
            Instant createdAt,
            Instant updateAt
    ) {}
}
