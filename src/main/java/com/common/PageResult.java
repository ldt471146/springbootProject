package com.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long current;
    private long size;
    private long total;
    private List<T> records;

    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    public static <T> PageResult<T> empty(long current, long size) {
        return new PageResult<>(current, size, 0, Collections.emptyList());
    }
}
