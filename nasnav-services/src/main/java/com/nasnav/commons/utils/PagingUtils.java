package com.nasnav.commons.utils;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class PagingUtils {

    public static PageRequest getQueryPage(Integer start, Integer count) {
        return PageRequest.of((int)Math.floor(start/count), count);
    }

    public static PageRequest getQueryPageAddIdSort(Integer start, Integer count) {
        return getQueryPageAddIdSort(start, count, Sort.unsorted());
    }

    public static PageRequest getQueryPageAddIdSort(Integer start, Integer count, Sort sort) {
        return PageRequest.of(start / count, count, sort.and(Sort.by(Direction.ASC, "id")));
    }
}
