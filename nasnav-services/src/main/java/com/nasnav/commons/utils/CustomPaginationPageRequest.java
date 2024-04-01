package com.nasnav.commons.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.io.Serializable;


@RequiredArgsConstructor
public class CustomPaginationPageRequest implements Pageable, Serializable {

    private static final long serialVersionUID = -25822477129613575L;

    private int limit;
    private int offset;
    private Sort sort;



    public CustomPaginationPageRequest(int offset, int limit, Sort sort) {

            if (offset < 0) {
                throw new IllegalArgumentException("Offset index must not be less than zero!");
            }

            if (limit < 1) {
                throw new IllegalArgumentException("Limit must not be less than one!");
            }
            this.limit = limit;
            this.offset = offset;
            this.sort = sort;
        }


    public CustomPaginationPageRequest(int offset, int limit, Sort.Direction direction, String... properties) {
        this(offset, limit, Sort.by(direction, properties));
    }

    public CustomPaginationPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return PageRequest.of(pageNumber, getPageSize(), sort);
    }

    @Override
    public int getPageNumber() {
        return  (offset / limit);
    }

    @Override
    public int getPageSize() {
        return (int) limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new CustomPaginationPageRequest(offset + limit, limit, sort);
    }

    public CustomPaginationPageRequest previous() {
        return hasPrevious() ? new CustomPaginationPageRequest(offset - limit, limit, sort) : this;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new CustomPaginationPageRequest(0, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomPaginationPageRequest)) return false;

        CustomPaginationPageRequest that = (CustomPaginationPageRequest) o;

        return new EqualsBuilder()
                .append(limit, that.limit)
                .append(offset, that.offset)
                .append(sort, that.sort)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(limit)
                .append(offset)
                .append(sort)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("limit", limit)
                .append("offset", offset)
                .append("sort", sort)
                .toString();
    }
}
