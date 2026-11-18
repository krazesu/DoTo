package com.kraze.doto.activity;

import com.kraze.doto.common.dto.PageResponse;
import com.kraze.doto.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService service;

    @GetMapping
    public PageResponse<ActivityResponse> getFeed(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @PathVariable("workspaceId") Long workspaceId,
                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "20") int size){
        return service.getFeed(workspaceId, user.id(), page, size);
    }
}
