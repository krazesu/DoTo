package com.kraze.doto.task;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TaskSpecifications {
    private TaskSpecifications() {}

    static Specification<Task> filter(
            Long workspaceId,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId,
            LocalDate dueBefore) {

        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("workspaceId"), workspaceId);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assigneeId"), assigneeId));
        }
        if (dueBefore != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.<LocalDate>get("dueDate"), dueBefore));
        }
        return spec;
    }
}
