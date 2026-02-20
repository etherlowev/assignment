package com.personal.assignment.filter;

import com.personal.assignment.enums.Direction;
import com.personal.assignment.model.request.Paging;
import org.springframework.data.relational.core.query.Criteria;

public abstract class FilteredPaging {
    private final int page;
    private final int perPage;
    private final String sort;
    private final Direction direction;

    public FilteredPaging(int page, int perPage, String sort, Direction direction) {
        this.page = page;
        this.perPage = perPage;
        this.sort = sort;
        this.direction = direction;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public String getSort() {
        return sort;
    }

    public Direction getDirection() {
        return direction;
    }

    public abstract Paging getPaging();

    public abstract Criteria getCriteria();
}
