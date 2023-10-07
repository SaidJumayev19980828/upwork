package com.nasnav.commons.criteria.data;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CrieteriaQueryResults<T> {
    private final List<T> resultList;
    private final Long resultCount;
}
