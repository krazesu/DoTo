package com.kraze.doto.task;

import com.kraze.doto.common.dto.PageResponse;
import com.kraze.doto.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.kraze.doto.task.TaskDtos.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final SubtaskService subtaskService;

    @PostMapping("/workspaces/{workspaceId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable("workspaceId") Long workspaceId,
                                   @Valid @RequestBody TaskDtos.CreateTaskRequest req){

        return taskService.createTask(workspaceId, user.id(), req);
    }

    @GetMapping("/workspaces/{workspaceId}/tasks")
    public PageResponse<TaskResponse> listTasks(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable("workspaceId") Long workspaceId,
                                                @RequestParam(name ="status", required = false) TaskStatus status,
                                                @RequestParam(name ="priority", required = false) TaskPriority priority,
                                                @RequestParam(name ="assigneeId", required = false) Long assigneeId,
                                                @RequestParam(name ="dueBefore", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
                                                @RequestParam(name ="page", defaultValue = "0") int page,
                                                @RequestParam(name ="size", defaultValue = "20") int size){

        return taskService.listTasks(workspaceId, user.id(), status, priority, assigneeId, dueBefore, page, size);
    }

    @GetMapping("/tasks/{taskId}")
    public TaskResponse getTask(@AuthenticationPrincipal AuthenticatedUser user,
                                @PathVariable("taskId") Long taskId){

        return taskService.getTask(taskId, user.id());
    }

    @PutMapping("/tasks/{taskId}")
    public TaskResponse updateTask(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable("taskId") Long taskId,
                                   @Valid @RequestBody UpdateTaskRequest req){

        return taskService.updateTask(taskId, user.id(), req);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public TaskResponse changeTaskStatus(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable("taskId") Long taskId,
                                         @Valid @RequestBody UpdateStatusRequest req){

        return taskService.changeTaskStatus(taskId, user.id(), req);
    }

    @PatchMapping("/tasks/{taskId}/assignee")
    public TaskResponse assignTask(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable("taskId") Long taskId,
                                   @Valid @RequestBody AssignRequest req){

        return taskService.assignTask(taskId, user.id(), req);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal AuthenticatedUser user,
                           @PathVariable("taskId")  Long taskId){

        taskService.deleteTask(taskId, user.id());
    }

    //Subtasks controllers
    @PostMapping("/tasks/{taskId}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    public SubtaskResponse addSubtask(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable("taskId") Long taskId,
                                      @Valid @RequestBody CreateSubtaskRequest req){

        return subtaskService.addSubtask(taskId, user.id(), req);
    }

    @PatchMapping("/subtasks/{subtaskId}")
    public SubtaskResponse updateSubtask(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable("subtaskId") Long subtask,
                                         @Valid @RequestBody UpdateSubtaskRequest req){

        return subtaskService.updateSubtask(subtask, user.id(), req);
    }

    @DeleteMapping("/subtasks/{subtaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubtask(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable("subtaskId") Long subtaskId){

        subtaskService.deleteSubtask(subtaskId, user.id());
    }
}
