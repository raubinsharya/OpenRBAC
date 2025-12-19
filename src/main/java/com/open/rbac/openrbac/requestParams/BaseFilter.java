package com.open.rbac.openrbac.RequestParams;

import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseFilter {

    // ---------------- Common date filters ----------------
    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime createdAfter;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime createdBefore;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime updatedAfter;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime updatedBefore;

    // ---------------- Pagination & Sorting ----------------
    @Builder.Default
    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String order = "DESC";

    /**
     * Converts filter to Spring Pageable
     */
    public @NonNull Pageable toPageable() {
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
