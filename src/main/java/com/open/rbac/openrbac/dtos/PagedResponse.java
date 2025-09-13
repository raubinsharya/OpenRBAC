package com.open.rbac.openrbac.dtos;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    /**
     * Maps a Spring Data Page<T> to PagedResponse<R> using a mapper function
     */
    public static <T, R> PagedResponse<R> fromPage(Page<T> page, Function<T, R> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Convenience method: returns a PagedResponse with the same type as the page content
     */
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return fromPage(page, Function.identity());
    }
}
