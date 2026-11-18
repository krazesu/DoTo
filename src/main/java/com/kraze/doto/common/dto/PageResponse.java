package com.kraze.doto.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        Long totalElements,
        int totalPage
    ){

    public static <T> PageResponse<T> of(List<T> content, Page<?> page){
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
