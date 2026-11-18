package com.kraze.doto.activity;

import com.kraze.doto.common.dto.UserSummary;

import java.time.Instant;

public record ActivityResponse(
        Long id,
        ActivityAction action,
        String targetType,
        Long targetId,
        String details,
        UserSummary actor,
        Instant createdAt
) {}
