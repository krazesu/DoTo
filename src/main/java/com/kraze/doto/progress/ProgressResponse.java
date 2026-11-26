package com.kraze.doto.progress;

import com.kraze.doto.task.TaskStatus;

import java.util.List;
import java.util.Map;

public record ProgressResponse(
        long totalTasks,
        long doneTasks,
        int completionPercent,
        long overdueTasks,
        long unassignedTasks,
        Map<TaskStatus, Long> countsByStatus,
        List<MemberProgress> members){

    public record MemberProgress(Long userId,
                                 String username,
                                 String displayName,
                                 long assigned,
                                 long done) {}
}
