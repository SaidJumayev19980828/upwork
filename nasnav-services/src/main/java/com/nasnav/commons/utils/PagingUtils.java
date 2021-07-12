package com.nasnav.commons.utils;

import org.springframework.data.domain.PageRequest;

public class PagingUtils {

    public static PageRequest getQueryPage(Integer start, Integer count) {
        return PageRequest.of((int)Math.floor(start/count), count);
    }
}
