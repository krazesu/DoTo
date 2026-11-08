package com.kraze.doto.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId,Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId,Long userId);
}
