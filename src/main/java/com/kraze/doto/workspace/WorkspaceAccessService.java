package com.kraze.doto.workspace;

import com.kraze.doto.common.exception.ForbiddenException;
import com.kraze.doto.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceAccessService {
    private final WorkspaceRepository workspaces;
    private final WorkspaceMemberRepository members;

    public WorkspaceMember requireMember(Long workspaceId, Long userId) {
        if(!workspaces.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found");
        }
        return members.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this workspace"));
    }

    public WorkspaceMember requireRole(Long workspaceId, Long userId, WorkspaceRole minimum) {
        WorkspaceMember member = requireMember(workspaceId, userId);
        if(!member.getRole().atLeast(minimum)) {
            throw new ForbiddenException("This action requires the " + minimum + " role");
        }
        return member;
    }
}
