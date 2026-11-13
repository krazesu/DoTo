package com.kraze.doto.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId,Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId,Long userId);

    List<WorkspaceMember> findByUserId(Long userId);

    @Query("""
            SELECT new com.kraze.doto.workspace.MemberResponse(
                u.id, u.username, u.displayName, m.role, m.joinedAt)
            FROM WorkspaceMember m
            JOIN User u on u.id = m.userId
            WHERE m.workspaceId = :workspaceId
            ORDER BY m.joinedAt
           """)
    List<MemberResponse> findMembers(@Param("workspaceId") Long workspaceId);
}
