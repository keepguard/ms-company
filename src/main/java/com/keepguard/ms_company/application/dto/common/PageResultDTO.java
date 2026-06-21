package com.keepguard.ms_company.application.dto.common;

import java.util.List;

public record PageResultDTO<T>(
    List<T> items,
    long total,
    int page,
    int size
) {

    public int getTotalPages() {
        return size > 0 ? (int) Math.ceil((double) total / size) : 0;
    }

    public boolean hasNext() {
        return page < getTotalPages() - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
