package com.personal.assignment.filter.impl;

import com.personal.assignment.enums.Direction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.filter.FilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.Paging;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.format.annotation.DateTimeFormat;

public class DocumentFilteredPaging extends FilteredPaging {

    private final List<String> authors;

    private final List<DocumentStatus> statuses;

    private final ZonedDateTime createdAfter;

    private final ZonedDateTime createdBefore;

    private final ZonedDateTime updatedAfter;

    private final ZonedDateTime updatedBefore;

    public DocumentFilteredPaging(@NotNull int page, @NotNull int perPage,
                                  @Nullable
                                  String sort,
                                  @Nullable
                                  Direction direction,
                                  @Nullable
                                  List<String> authors,
                                  @Nullable
                                  List<DocumentStatus> statuses,
                                  @Nullable
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  ZonedDateTime createdAfter,
                                  @Nullable
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  ZonedDateTime createdBefore,
                                  @Nullable
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  ZonedDateTime updatedAfter,
                                  @Nullable
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  ZonedDateTime updatedBefore) {
        super(page, perPage, sort, direction);
        this.authors = authors;
        this.statuses = statuses;
        this.createdAfter = createdAfter;
        this.createdBefore = createdBefore;
        this.updatedAfter = updatedAfter;
        this.updatedBefore = updatedBefore;
    }

    @Override
    public Paging getPaging() {
        return new Paging(getPage(), getPerPage(), getSort(), getDirection());
    }

    @Override
    public Criteria getCriteria() {
        Criteria criteria = Criteria.empty();
        if (authors != null && !authors.isEmpty()) {
            criteria = criteria.and(Criteria.where(Document.DOCUMENT_AUTHOR).in(authors));
        }
        if (statuses != null && !statuses.isEmpty()) {
            criteria = criteria.and(Criteria.where(Document.DOCUMENT_STATUS).in(statuses));
        }
        if (createdAfter != null) {
            criteria = criteria.and(Criteria.where(Document.DOCUMENT_DATE_CREATED)
                .greaterThan(createdAfter));
        }
        if (createdBefore != null) {
            criteria = criteria.and(Criteria.where(Document.DOCUMENT_DATE_CREATED)
                .lessThan(createdBefore));
        }
        if (updatedAfter != null) {
            criteria = criteria.and(Criteria.where(Document.DOCUMENT_DATE_UPDATED)
                .greaterThan(updatedAfter));
        }
        if (updatedBefore != null) {
            criteria = criteria.and(Criteria.where(Document.DOCUMENT_DATE_UPDATED)
                .lessThan(updatedBefore));
        }
        return criteria;
    }
}
