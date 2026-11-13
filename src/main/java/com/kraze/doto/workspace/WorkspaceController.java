package com.kraze.doto.workspace;

import com.kraze.doto.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateWorkspaceRequest req){
        return service.createWorkspace(user.id(), req);
    }

    @GetMapping
    public List<WorkspaceResponse> MyWorkspaces(@AuthenticationPrincipal AuthenticatedUser user){
        return service.listMyWorkspaces(user.id());
    }

    @GetMapping("/{id}")
    public WorkspaceResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("id") Long id){
        return service.getWorkspace(id, user.id());
    }

    @PutMapping("/{id}")
    public WorkspaceResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("id") Long id, @Valid @RequestBody UpdateWorkspaceRequest req){
        return service.updateWorkspace(id, user.id(), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("id") Long id){
        service.deleteWorkspace(id, user.id());
    }

    @PostMapping("/join")
    public WorkspaceResponse join(@AuthenticationPrincipal AuthenticatedUser user,@Valid @RequestBody JoinWorkspaceRequest req){
        return service.joinWorkspace(user.id(), req);
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> members(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("id") Long id){
        return service.listMembers(id, user.id());
    }

    @PatchMapping("/{id}/members/{userId}")
    public MemberResponse changeRole(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("id") Long id, @PathVariable("userId") Long targetUserId,
                                     @Valid @RequestBody ChangeRoleRequest req){
        return service.changeRole(id, user.id(), targetUserId, req);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("id") Long id, @PathVariable("userId") Long targetUserId){
        service.removeMember(id, user.id(), targetUserId);
    }
}
