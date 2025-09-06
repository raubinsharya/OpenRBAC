package com.open.rbac.openrbac.dtos;

import java.util.function.Function;
import org.springframework.data.domain.Page;

public class PagedResponseMapper {

    public static <T, R> PagedResponse<R> fromPage(Page<T> page, Function<T, R> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
