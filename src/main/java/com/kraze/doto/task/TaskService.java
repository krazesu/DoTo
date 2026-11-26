package com.kraze.doto.task;

import com.kraze.doto.activity.ActivityAction;
import com.kraze.doto.activity.ActivityService;
import com.kraze.doto.common.dto.PageResponse;
import com.kraze.doto.common.dto.UserSummary;
import com.kraze.doto.common.exception.BadRequestException;
import com.kraze.doto.common.exception.ConflictException;
import com.kraze.doto.common.exception.ForbiddenException;
import com.kraze.doto.common.exception.ResourceNotFoundException;
import com.kraze.doto.user.User;
import com.kraze.doto.user.UserRepository;
import com.kraze.doto.workspace.WorkspaceAccessService;
import com.kraze.doto.workspace.WorkspaceMember;
import com.kraze.doto.workspace.WorkspaceMemberRepository;
import com.kraze.doto.workspace.WorkspaceRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.kraze.doto.task.TaskDtos.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository tasks;
    private final SubtaskRepository subtasks;
    private final UserRepository users;
    private final WorkspaceMemberRepository members;
    private final WorkspaceAccessService access;
    private final ActivityService activity;

    @Transactional
    public TaskResponse createTask(Long workspaceId, Long userId, CreateTaskRequest req){
        access.requireMember(workspaceId, userId);

        Task task = new Task();
        task.setWorkspaceId(workspaceId);
        task.setTitle(req.title().trim());
        task.setDescription(req.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(req.priority() != null? req.priority(): TaskPriority.LOW);
        task.setDueDate(req.dueDate());
        task.setCreatedBy(userId);
        if(req.assigneeId() != null){
            requireAssignable(workspaceId, req.assigneeId());
            task.setAssigneeId(req.assigneeId());
        }
        tasks.save(task);

        activity.record(workspaceId, userId, ActivityAction.TASK_CREATED, "TASK",task.getId(), "Created \"" + task.getTitle() + "\"");
        return single(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> listTasks(Long workspaceId, Long userId, TaskStatus status,
                                                TaskPriority priority, Long assigneeId, LocalDate dueBefore,
                                                int page, int size){
        access.requireMember(workspaceId, userId);

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<Task> result = tasks.findAll(
                TaskSpecifications.filter(workspaceId, status, priority, assigneeId, dueBefore), pageable);

        return PageResponse.of(assemble(result.getContent()), result);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId, long userId){
        Task task = findTask(taskId);
        access.requireMember(task.getWorkspaceId(), userId);

        return single(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, Long userId, UpdateTaskRequest req){
        Task task = findTask(taskId);
        access.requireMember(task.getWorkspaceId(), userId);

        if(!task.getVersion().equals(req.version())){
            throw new ConflictException("This task was changed by someone else. Reload and try again.");
        }
        task.setTitle(req.title().trim());
        task.setDescription(req.description());
        task.setPriority(req.priority());
        task.setDueDate(req.dueDate());
        tasks.saveAndFlush(task);

        activity.record(task.getWorkspaceId(), userId, ActivityAction.TASK_UPDATED, "TASK",task.getId(), "Edited \"" + task.getTitle() + "\"");
        return single(task);
    }

    @Transactional
    public TaskResponse changeTaskStatus(Long taskId, Long userId, UpdateStatusRequest req){
        Task task = findTask(taskId);
        access.requireMember(task.getWorkspaceId(), userId);

        TaskStatus oldStatus = task.getStatus();
        if(oldStatus == req.status()){
            return single(task);
        }

        task.setStatus(req.status());
        tasks.saveAndFlush(task);

        activity.record(task.getWorkspaceId(), userId, ActivityAction.TASK_STATUS_CHANGED, "TASK", task.getId(),
                "Moved \"" + task.getTitle() + "\" from " + oldStatus + " to " + req.status());

        return single(task);
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, Long userId, AssignRequest req){
        Task task = findTask(taskId);
        access.requireMember(task.getWorkspaceId(), userId);

        Long newAssignee = req.assigneeId();
        if(Objects.equals(newAssignee, task.getAssigneeId())){
            return single(task);
        }

        String details;
        if(newAssignee == null){
            details = "Unassigned \"" + task.getTitle() + "\"";
        }
        else{
            requireAssignable(task.getWorkspaceId(), newAssignee);
            String name = users.findById(newAssignee).map(User::getUsername).orElse("user " + newAssignee);
            details = "Assigned \"" + task.getTitle() + "\" to " + name;
        }

        task.setAssigneeId(newAssignee);
        tasks.saveAndFlush(task);

        activity.record(task.getWorkspaceId(), userId, ActivityAction.TASKS_ASSIGNED, "TASK", task.getId(), details);
        return single(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId){
        Task task = findTask(taskId);
        WorkspaceMember me = access.requireMember(task.getWorkspaceId(), userId);

        boolean isCreator = task.getCreatedBy().equals(userId);
        if(!isCreator && !me.getRole().atLeast(WorkspaceRole.ADMIN)){
            throw new ForbiddenException("You are not allowed to delete this task");
        }

        activity.record(task.getWorkspaceId(), userId, ActivityAction.TASK_DELETED, "TASK", task.getId(), "Deleted \"" + task.getTitle() + "\"");
        tasks.delete(task);
    }

    //Helper functions

    private Task findTask(Long taskId){
        return tasks.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void requireAssignable(Long workspaceId, Long assigneeId) {
        if(!members.existsByWorkspaceIdAndUserId(workspaceId, assigneeId)){
            throw new BadRequestException("The assignee must be a member of this workspace");
        }
    }

    private TaskResponse single(Task task){
        return assemble(List.of(task)).get(0);
    }

    private List<TaskResponse> assemble(List<Task> taskList){
        if(taskList.isEmpty()){
            return List.of();
        }

        List<Long> taskIds = taskList.stream().map(Task::getId).toList();
        Map<Long, List<Subtask>> subtasksByTask = subtasks.findByTaskIdInOrderByIdAsc(taskIds).stream()
                .collect(Collectors.groupingBy(Subtask::getTaskId));

        Set<Long> userIds = new HashSet<>();
        for(Task t:  taskList){
            userIds.add(t.getCreatedBy());
            if(t.getAssigneeId() != null){
                userIds.add(t.getAssigneeId());
            }
        }

        Map<Long, UserSummary> userMap = users.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> new UserSummary(u.getId(), u.getUsername(), u.getDisplayName())));

        return taskList.stream()
                .map(t -> toResponse(t, subtasksByTask.getOrDefault(t.getId(), List.of()), userMap))
                .toList();
    }

    private TaskResponse toResponse(Task t, List<Subtask> subs, Map<Long, UserSummary> userMap) {
        int done = (int) subs.stream().filter(Subtask::isDone).count();

        boolean overdue = t.getDueDate() != null
                && t.getStatus() != TaskStatus.DONE
                && t.getDueDate().isBefore(LocalDate.now());
        List<SubtaskResponse> subResponses = subs.stream()
                .map(s -> new SubtaskResponse(s.getId(), s.getTaskId(), s.getTitle(), s.isDone()))
                .toList();

        return new TaskResponse(
                t.getId(), t.getWorkspaceId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getDueDate(), overdue,
                t.getAssigneeId() == null ? null : userMap.get(t.getAssigneeId()),
                userMap.get(t.getCreatedBy()),
                subResponses,
                ProgressCalculator.forTask(t.getStatus(), subs.size(), done),
                t.getVersion(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
