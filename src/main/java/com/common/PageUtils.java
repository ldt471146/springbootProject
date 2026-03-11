package com.common;

import java.util.Collections;
import java.util.List;

public final class PageUtils {

    private PageUtils() {
    }

    public static <T> PageResult<T> fromList(List<T> source, int page, int size) {
        if (source == null || source.isEmpty()) {
            return PageResult.empty(page, size);
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int fromIndex = (safePage - 1) * safeSize;
        if (fromIndex >= source.size()) {
            return new PageResult<>(safePage, safeSize, source.size(), Collections.emptyList());
        }
        int toIndex = Math.min(fromIndex + safeSize, source.size());
        return new PageResult<>(safePage, safeSize, source.size(), source.subList(fromIndex, toIndex));
    }
}
