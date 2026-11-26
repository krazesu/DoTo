package com.kraze.doto.progress;

import com.kraze.doto.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/progress")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService progressService;

    @GetMapping
    public ProgressResponse summary(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable("workspaceId") Long workspaceId){

        return progressService.summary(workspaceId, user.id());
    }
}
