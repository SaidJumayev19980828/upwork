package com.nasnav.commons.criteria;

import com.nasnav.commons.criteria.data.CrieteriaQueryResults;
import com.nasnav.request.BaseSearchParams;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public abstract class AbstractCriteriaQueryBuilder<T, P extends BaseSearchParams> {
    private final Class<T> theClass;
    final EntityManager entityManager;

    AbstractCriteriaQueryBuilder(EntityManager entityManager, Class<T> theClass) {
        this.entityManager = entityManager;
        this.theClass = theClass;
    }

    public CrieteriaQueryResults<T> getResultList(P searchParams, boolean count) {

        CriteriaQueryContext<T, P> context = buildQuery(searchParams);
        List<T> results = queryForList(context.getQuery(), searchParams);

        Long total = null;
        if (count) {
            total = queryForCount(context.getCriteriaBuilder(), context.getPredicates());
        }

        return new CrieteriaQueryResults<>(results, total);
    }

    private CriteriaQueryContext<T, P> buildQuery(P searchParams) {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        CriteriaQuery<T> query = getCriteriaQuery(criteriaBuilder);
        Root<T> root = getRoot(query);
        Predicate[] predicates = getPredicates(criteriaBuilder, root, searchParams);
        CriteriaQueryContext<T, P> context = CriteriaQueryContext.<T, P>builder()
                .searchParams(searchParams)
                .criteriaBuilder(criteriaBuilder)
                .query(query)
                .root(root)
                .predicates(predicates)
                .build();
        updateQueryWithConditionAndOrderBy(context);
        return context;
    }

    private CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    private CriteriaQuery<T> getCriteriaQuery(CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.createQuery(theClass);
    }

    private Long queryForCount(CriteriaBuilder criteriaBuilder, Predicate[] predicates) {
        CriteriaQuery<Long> countQuery = entityManager.getCriteriaBuilder().createQuery(Long.class);
        Root<T> root = countQuery.from(theClass);
        countQuery.select(criteriaBuilder.count(root)).where(predicates);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    abstract Root<T> getRoot(CriteriaQuery<T> query);

    abstract Predicate[] getPredicates(CriteriaBuilder criteriaBuilder, Root<T> root, P searchParams);

    abstract void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<T, P> context);

    abstract List<T> queryForList(CriteriaQuery<T> query, P searchParams);
}

@Getter
@Builder
class CriteriaQueryContext<T, P extends BaseSearchParams> {
    private final P searchParams;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<T> query;
    private final Root<T> root;
    private final Predicate[] predicates;
}