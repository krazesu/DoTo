package com.kraze.doto.activity;

import com.kraze.doto.common.dto.PageResponse;
import com.kraze.doto.common.dto.UserSummary;
import com.kraze.doto.user.User;
import com.kraze.doto.user.UserRepository;
import com.kraze.doto.workspace.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityLogRepository logs;
    private final UserRepository users;
    private final WorkspaceAccessService access;

    //commit with transaction, record to activity logs
    public void record(Long workspaceId, long actorId, ActivityAction action,
                       String targetType, Long targetId, String details) {
        ActivityLog log = new ActivityLog();
        log.setWorkspaceId(workspaceId);
        log.setActorId(actorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details != null ? details.length() > 500 ? details.substring(0, 500) : details: null);
        logs.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityResponse> getFeed(Long workspaceId, Long userId, int page, int size) {
        access.requireMember(workspaceId, userId);

        //pagination and sorting
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        //retrieve paginated activity logs for the workspace
        Page<ActivityLog> result = logs.findByWorkspaceId(workspaceId, pageable);

        //extract unique user ids of actors for each activities
        Set<Long> actorIds = result.getContent().stream()
                .map(ActivityLog::getActorId)
                .collect(Collectors.toSet());

        //fetch actors details and map them by user id
        Map<Long, UserSummary> actors = users.findAllById(actorIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> new UserSummary(
                                u.getId(),
                                u.getUsername(),
                                u.getDisplayName()
                        )
                )
        );

        //convert into response DTO
        List<ActivityResponse> content = result.getContent().stream()
                .map(l -> new ActivityResponse(
                        l.getId(),
                        l.getAction(),
                        l.getTargetType(),
                        l.getTargetId(),
                        l.getDetails(),
                        actors.get(l.getActorId()),
                        l.getCreatedAt()))
                .toList();

        //return activities with pagination metadata
        return PageResponse.of(content, result);
    }
}
