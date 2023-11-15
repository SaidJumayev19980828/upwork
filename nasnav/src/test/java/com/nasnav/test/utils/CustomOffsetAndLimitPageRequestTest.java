package com.nasnav.test.utils;

import com.nasnav.commons.utils.CustomOffsetAndLimitPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Sort;

class CustomOffsetAndLimitPageRequestTest {

    @Test
    void testConstructorWithValidValues() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(0, 10, Sort.unsorted());

        Assertions.assertEquals(0, pageRequest.getOffset());
        Assertions.assertEquals(10, pageRequest.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), pageRequest.getSort());
    }

    @Test
    void testConstructorWithInvalidOffset() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomOffsetAndLimitPageRequest(-1, 10, Sort.unsorted());
        });
    }

    @Test
    void testConstructorWithInvalidLimit() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomOffsetAndLimitPageRequest(0, 0, Sort.unsorted());
        });
    }

    @Test
    void testGetPageNumber() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(20, 10, Sort.unsorted());

        Assertions.assertEquals(2, pageRequest.getPageNumber());
    }

    @Test
    void testNext() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(0, 10, Sort.unsorted());
        CustomOffsetAndLimitPageRequest nextPage = (CustomOffsetAndLimitPageRequest) pageRequest.next();

        Assertions.assertEquals(10, nextPage.getOffset());
        Assertions.assertEquals(10, nextPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), nextPage.getSort());
    }

    @Test
    void testPrevious() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(20, 10, Sort.unsorted());
        CustomOffsetAndLimitPageRequest previousPage = pageRequest.previous();

        Assertions.assertEquals(10, previousPage.getOffset());
        Assertions.assertEquals(10, previousPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), previousPage.getSort());
    }

    @Test
    void testPreviousOrFirstWithPrevious() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(20, 10, Sort.unsorted());
        CustomOffsetAndLimitPageRequest previousOrFirstPage = (CustomOffsetAndLimitPageRequest) pageRequest.previousOrFirst();

        Assertions.assertEquals(10, previousOrFirstPage.getOffset());
        Assertions.assertEquals(10, previousOrFirstPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), previousOrFirstPage.getSort());
    }

    @Test
    void testPreviousOrFirstWithoutPrevious() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(0, 10, Sort.unsorted());
        CustomOffsetAndLimitPageRequest previousOrFirstPage = (CustomOffsetAndLimitPageRequest) pageRequest.previousOrFirst();

        Assertions.assertEquals(0, previousOrFirstPage.getOffset());
        Assertions.assertEquals(10, previousOrFirstPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), previousOrFirstPage.getSort());
    }

    @Test
    void testFirst() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(20, 10, Sort.unsorted());
        CustomOffsetAndLimitPageRequest firstPage = (CustomOffsetAndLimitPageRequest) pageRequest.first();

        Assertions.assertEquals(0, firstPage.getOffset());
        Assertions.assertEquals(10, firstPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), firstPage.getSort());
    }

    @Test
    void testHasPreviousWithPrevious() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(20, 10, Sort.unsorted());

        Assertions.assertTrue(pageRequest.hasPrevious());
    }

    @Test
    void testHasPreviousWithoutPrevious() {
        CustomOffsetAndLimitPageRequest pageRequest = new CustomOffsetAndLimitPageRequest(0, 10, Sort.unsorted());

        Assertions.assertFalse(pageRequest.hasPrevious());
    }

    @Test
    void testEqualsAndHashCode() {
        CustomOffsetAndLimitPageRequest pageRequest1 = new CustomOffsetAndLimitPageRequest(0, 10, Sort.unsorted());
        CustomOffsetAndLimitPageRequest pageRequest2 = new CustomOffsetAndLimitPageRequest(0, 10, Sort.unsorted());

        Assertions.assertEquals(pageRequest1, pageRequest2);
        Assertions.assertEquals(pageRequest1.hashCode(), pageRequest2.hashCode());
    }
}