package com.kraze.doto.workspace;

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
