package com.nasnav.test.utils;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Sort;

class CustomPaginationPageRequestTest {

    @Test
    void testConstructorWithValidValues() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(0, 10, Sort.unsorted());

        Assertions.assertEquals(0, pageRequest.getOffset());
        Assertions.assertEquals(10, pageRequest.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), pageRequest.getSort());
    }

    @Test
    void testConstructorWithInvalidOffset() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomPaginationPageRequest(-1, 10, Sort.unsorted());
        });
    }

    @Test
    void testConstructorWithInvalidLimit() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomPaginationPageRequest(0, 0, Sort.unsorted());
        });
    }

    @Test
    void testGetPageNumber() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(20, 10, Sort.unsorted());

        Assertions.assertEquals(2, pageRequest.getPageNumber());
    }

    @Test
    void testNext() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(0, 10, Sort.unsorted());
        CustomPaginationPageRequest nextPage = (CustomPaginationPageRequest) pageRequest.next();

        Assertions.assertEquals(10, nextPage.getOffset());
        Assertions.assertEquals(10, nextPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), nextPage.getSort());
    }

    @Test
    void testPrevious() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(20, 10, Sort.unsorted());
        CustomPaginationPageRequest previousPage = pageRequest.previous();

        Assertions.assertEquals(10, previousPage.getOffset());
        Assertions.assertEquals(10, previousPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), previousPage.getSort());
    }

    @Test
    void testPreviousOrFirstWithPrevious() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(20, 10, Sort.unsorted());
        CustomPaginationPageRequest previousOrFirstPage = (CustomPaginationPageRequest) pageRequest.previousOrFirst();

        Assertions.assertEquals(10, previousOrFirstPage.getOffset());
        Assertions.assertEquals(10, previousOrFirstPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), previousOrFirstPage.getSort());
    }

    @Test
    void testPreviousOrFirstWithoutPrevious() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(0, 10, Sort.unsorted());
        CustomPaginationPageRequest previousOrFirstPage = (CustomPaginationPageRequest) pageRequest.previousOrFirst();

        Assertions.assertEquals(0, previousOrFirstPage.getOffset());
        Assertions.assertEquals(10, previousOrFirstPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), previousOrFirstPage.getSort());
    }

    @Test
    void testFirst() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(20, 10, Sort.unsorted());
        CustomPaginationPageRequest firstPage = (CustomPaginationPageRequest) pageRequest.first();

        Assertions.assertEquals(0, firstPage.getOffset());
        Assertions.assertEquals(10, firstPage.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), firstPage.getSort());
    }

    @Test
    void testHasPreviousWithPrevious() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(20, 10, Sort.unsorted());

        Assertions.assertTrue(pageRequest.hasPrevious());
    }

    @Test
    void testHasPreviousWithoutPrevious() {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(0, 10, Sort.unsorted());

        Assertions.assertFalse(pageRequest.hasPrevious());
    }

    @Test
    void testEqualsAndHashCode() {
        CustomPaginationPageRequest pageRequest1 = new CustomPaginationPageRequest(0, 10, Sort.unsorted());
        CustomPaginationPageRequest pageRequest2 = new CustomPaginationPageRequest(0, 10, Sort.unsorted());

        Assertions.assertEquals(pageRequest1, pageRequest2);
        Assertions.assertEquals(pageRequest1.hashCode(), pageRequest2.hashCode());
    }
}