package com.personal.assignment.model.request;

import com.personal.assignment.enums.Direction;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class Paging implements Pageable {
    private final int page;
    private final int perPage;
    private final String sort;
    private final Direction direction;

    public Paging(int page, int perPage, String sort, Direction direction) {
        this.page = page;
        this.perPage = perPage;
        this.sort = sort;
        this.direction = direction;
    }

    @Override
    public int getPageNumber() {
        return page;
    }

    @Override
    public int getPageSize() {
        return perPage;
    }

    @Override
    public long getOffset() {
        return Math.max(0, (long) (page - 1) * perPage);
    }

    @Override
    public @NonNull Sort getSort() {
        return this.getSortOr(Sort.unsorted());
    }

    @Override
    public @NonNull Sort getSortOr(@NonNull Sort sort) {
        if (this.sort == null || this.direction == null) {
            return sort;
        }
        return Sort.by(direction == Direction.ASC ? Sort.Order.asc(this.sort) : Sort.Order.desc(this.sort));
    }

    @Override
    public @NonNull Pageable next() {
        return new Paging(page+1, perPage, sort, direction);
    }

    @Override
    public @NonNull Pageable previousOrFirst() {
        if (this.page <= 1) {
            return new Paging(1, perPage, sort, direction);
        }
        return new Paging(page-1, perPage, sort, direction);
    }

    @Override
    public @NonNull Pageable first() {
        return new Paging(1, this.perPage, this.sort, this.direction);
    }

    @Override
    public @NonNull Pageable withPage(int pageNumber) {
        return new Paging(pageNumber, this.perPage, this.sort, this.direction);
    }

    @Override
    public boolean hasPrevious() {
        return page > 1;
    }
}
