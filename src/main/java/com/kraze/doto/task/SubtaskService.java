package com.kraze.doto.task;

import com.kraze.doto.activity.ActivityAction;
import com.kraze.doto.activity.ActivityService;
import com.kraze.doto.common.exception.BadRequestException;
import com.kraze.doto.common.exception.ResourceNotFoundException;
import com.kraze.doto.workspace.WorkspaceAccessService;
import com.kraze.doto.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kraze.doto.task.TaskDtos.*;

@Service
@RequiredArgsConstructor
public class SubtaskService {
    private final TaskRepository tasks;
    private final SubtaskRepository subtasks;
    private final WorkspaceAccessService access;
    private final ActivityService activity;

    @Transactional
    public SubtaskResponse addSubtask(Long taskId, long userId, CreateSubtaskRequest req){
        Task task = findTask(taskId);
        access.requireMember(task.getWorkspaceId(), userId);

        Subtask subtask = new Subtask();
        subtask.setTaskId(taskId);
        subtask.setTitle(req.title().trim());
        subtask.setDone(false);
        subtasks.save(subtask);

        activity.record(task.getWorkspaceId(), userId, ActivityAction.SUBTASK_ADDED, "SUBTASK", subtask.getId(),
                "Added \"" + subtask.getTitle() + "\" to \"" + task.getTitle() + "\"");

        return toResponse(subtask);
    }

    @Transactional
    public SubtaskResponse updateSubtask(Long subtaskId, long userId, UpdateSubtaskRequest req){
        Subtask subtask = findSubtask(subtaskId);
        Task task = findTask(subtask.getTaskId());
        access.requireMember(task.getWorkspaceId(), userId);

        StringBuilder details = new StringBuilder();
        if(req.title() != null){
            if(req.title().isBlank()){
                throw new BadRequestException("Subtask title cannot be blank");
            }
            subtask.setTitle(req.title().trim());
            details.append("Renamed subtask to \"").append(subtask.getTitle()).append("\"");
        }

        if(req.done() != null && req.done() != subtask.isDone()){
            subtask.setDone(req.done());
            if(!details.isEmpty()){
                details.append("; ");
            }
            details.append(req.done() ? "Completed" : "Reopened").append(" subtask \"").append(subtask.getTitle()).append("\"");
        }

        if (!details.isEmpty()) {
            activity.record(task.getWorkspaceId(), userId, ActivityAction.SUBTASK_UPDATED, "SUBTASK",
                    subtask.getId(), details.toString());
        }
        return toResponse(subtask);
    }

    @Transactional
    public void deleteSubtask(Long subtaskId, long userId) {
        Subtask subtask = findSubtask(subtaskId);
        Task task = findTask(subtask.getTaskId());
        access.requireMember(task.getWorkspaceId(), userId);

        activity.record(task.getWorkspaceId(), userId, ActivityAction.SUBTASK_DELETED, "SUBTASK", subtask.getId(),
                "Removed subtask \"" + subtask.getTitle() + "\" from \"" + task.getTitle() + "\"");

        subtasks.delete(subtask);
    }

    // Helper functions
    private Task findTask(Long id) {
        return tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private Subtask findSubtask(Long id) {
        return subtasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
    }

    private SubtaskResponse toResponse(Subtask subtask) {
        return new SubtaskResponse(subtask.getId(), subtask.getTaskId(), subtask.getTitle(), subtask.isDone());
    }
}
