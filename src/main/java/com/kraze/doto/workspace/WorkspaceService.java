package com.kraze.doto.workspace;

import com.kraze.doto.common.exception.BadRequestException;
import com.kraze.doto.common.exception.ConflictException;
import com.kraze.doto.common.exception.ForbiddenException;
import com.kraze.doto.common.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final WorkspaceRepository workspaces;
    private final WorkspaceMemberRepository members;
    private final WorkspaceAccessService access;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, CreateWorkspaceRequest req){
        Workspace w = new Workspace();
        w.setName(req.name().trim());
        w.setDescription(req.description());
        w.setOwnerId(userId);
        w.setInviteCode(generateUniqueCode());
        workspaces.save(w);

        members.save(new WorkspaceMember(w.getId(), userId, WorkspaceRole.OWNER));
        return toResponse(w, WorkspaceRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listMyWorkspaces(Long userId){
        Map<Long, WorkspaceRole> roles = members.findByUserId(userId).stream()
                .collect(Collectors.toMap(WorkspaceMember::getWorkspaceId, WorkspaceMember::getRole));

        return workspaces.findAllById(roles.keySet()).stream()
                .sorted(Comparator.comparing(Workspace::getCreatedAt).reversed())
                .map(w -> toResponse(w, roles.get(w.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId, Long userId){
        WorkspaceMember me = access.requireMember(workspaceId, userId);
        return toResponse(findWorkspace(workspaceId), me.getRole());
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, Long userId, UpdateWorkspaceRequest req){
        WorkspaceMember me = access.requireRole(workspaceId, userId, WorkspaceRole.ADMIN);
        Workspace w = findWorkspace(workspaceId);
        w.setName(req.name().trim());
        w.setDescription(req.description());
        return toResponse(w, me.getRole());
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId, Long userId){
        access.requireRole(workspaceId, userId, WorkspaceRole.OWNER);
        workspaces.deleteById(workspaceId);
    }

    @Transactional
    public WorkspaceResponse joinWorkspace(Long userId, JoinWorkspaceRequest req){
        Workspace w = workspaces.findByInviteCode(req.inviteCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite code"));

        if (members.existsByWorkspaceIdAndUserId(w.getId(), userId)){
            throw new ConflictException("You are already a member of this workspace");
        }

        members.save(new WorkspaceMember(w.getId(), userId, WorkspaceRole.MEMBER));
        return toResponse(w, WorkspaceRole.MEMBER);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long workspaceId, Long userId){
        access.requireMember(workspaceId, userId);
        return members.findMembers(workspaceId);
    }

    @Transactional
    public MemberResponse changeRole(long workspaceId, Long actorId, Long targetUserId, ChangeRoleRequest req){
        access.requireRole(workspaceId, actorId, WorkspaceRole.OWNER);
        if (req.role() == WorkspaceRole.OWNER){
            throw new BadRequestException("Transferring ownership not supported yet");
        }

        WorkspaceMember target = members.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (target.getRole() == WorkspaceRole.OWNER){
            throw new ConflictException("The owner's role cannot be changed");
        }

        target.setRole(req.role());
        return members.findMembers(workspaceId).stream()
                .filter(m -> m.userId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    @Transactional
    public void removeMember(Long workspaceId, Long actorId, Long targetUserId){
        WorkspaceMember actor = access.requireMember(workspaceId, actorId);
        WorkspaceMember target = members.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if(target.getRole() == WorkspaceRole.OWNER){
            throw new ConflictException("The owner cannot be removed");
        }
        boolean leavingSelf = actorId.equals(targetUserId);
        if (!leavingSelf){
            if(!actor.getRole().atLeast(WorkspaceRole.ADMIN)){
                throw new ForbiddenException("Only ADMIN and OWNER can remove members");
            }
            if(target.getRole() == WorkspaceRole.ADMIN && actor.getRole() != WorkspaceRole.OWNER){
                throw new ForbiddenException("Only OWNER can remove and admin");
            }
        }
        members.delete(target);
    }

    //Helper functions
    private Workspace findWorkspace(Long workspaceId){
        return workspaces.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
    }

    private WorkspaceResponse toResponse(Workspace w, WorkspaceRole myRole){
        String code = myRole.atLeast(WorkspaceRole.ADMIN) ? w.getInviteCode() : null;
        return new WorkspaceResponse(w.getId(), w.getName(), w.getDescription(), code, w.getOwnerId(), w.getCreatedAt(), myRole);
    }

    private String generateUniqueCode(){
        String code;
        do{
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for(int i = 0; i < CODE_LENGTH; i++){
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        }
        while(workspaces.existsByInviteCode(code));
        return code;
    }
}
