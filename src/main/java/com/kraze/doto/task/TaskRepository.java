package com.kraze.doto.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    interface StatusCount{
        TaskStatus getStatus();
        Long getTotal();
    }

    interface AssigneeStatusCount{
        Long getUserId();
        TaskStatus getStatus();
        Long getTotal();
    }

    @Query("""
            SELECT t.status as status, COUNT(t) as total
            FROM Task t
            WHERE t.workspaceId = :workspaceId
            GROUP BY t.status
            """)
    List<StatusCount> getStatusCounts(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT t.assigneeId as userId, t.status as status, COUNT(t) as total
            FROM Task t
            WHERE t.workspaceId = :workspaceId AND t.assigneeId IS NOT NULL
            GROUP BY t.assigneeId, t.status
            """)
    List<AssigneeStatusCount> getAssigneeStatusCounts(@Param("workspaceId") Long workspaceId);

    long countByWorkspaceIdAndStatusNotAndDueDateBefore(
            Long workspaceId,
            TaskStatus status,
            LocalDate date
    );

    long countByWorkspaceIdAndAssigneeIdIsNull(Long workspaceId);

    @Modifying
    @Query("""
            UPDATE Task t
            SET t.assigneeId = NULL
            WHERE t.workspaceId = :workspaceId AND t.assigneeId = :userId
            """)
    int unassignUser(@Param("workspaceId") Long workspaceId, @Param("userId") Long userId);
}
