package com.kraze.doto.progress;

import com.kraze.doto.progress.ProgressResponse.MemberProgress;
import com.kraze.doto.task.TaskRepository;
import com.kraze.doto.task.TaskStatus;
import com.kraze.doto.workspace.Workspace;
import com.kraze.doto.workspace.WorkspaceAccessService;
import com.kraze.doto.workspace.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final TaskRepository tasks;
    private final WorkspaceMemberRepository members;
    private final WorkspaceAccessService access;

    @Transactional(readOnly = true)
    public ProgressResponse summary(Long workspaceId, Long userId){
        access.requireMember(workspaceId, userId);

        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, 0L);
        }

        tasks.getStatusCounts(workspaceId).forEach(r -> counts.put(r.getStatus(), r.getTotal()));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long done = counts.get(TaskStatus.DONE);
        int completion = total == 0 ? 0 : (int) Math.round(100 * done/total);

        long overdue = tasks.countByWorkspaceIdAndStatusNotAndDueDateBefore(workspaceId, TaskStatus.DONE, LocalDate.now());
        long unassigned = tasks.countByWorkspaceIdAndAssigneeIdIsNull(workspaceId);

        Map<Long, long[]> perUser = new HashMap<>();
        for(var row: tasks.getAssigneeStatusCounts(workspaceId)){
            long[] a = perUser.computeIfAbsent(row.getUserId(), k -> new long[2]);
            a[0] += row.getTotal();
            if(row.getStatus() == TaskStatus.DONE){
                a[1] += row.getTotal();
            }
        }

        List<MemberProgress> memberStats = members.findMembers(workspaceId).stream()
                .map(m -> {
                    long[] a = perUser.getOrDefault(m.userId(), new long[2]);
                    return new MemberProgress(m.userId(), m.username(), m.displayName(), a[0], a[1]);
                })
                .toList();

        return new ProgressResponse(total, done, completion, overdue, unassigned, counts, memberStats);
    }
}
