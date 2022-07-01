package com.nasnav.commons.criteria;

import com.nasnav.request.BaseSearchParams;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

import static java.util.Objects.isNull;

public abstract class AbstractCriteriaQueryBuilder<T> {
    private Class<T> theClass;
    private Long resultCount;
    List<T> resultList;
    EntityManager entityManager;
    CriteriaBuilder builder;
    CriteriaQuery<T> query;
    Root<T> root;
    BaseSearchParams searchParams;
    String orderBy;
    Predicate [] predicates;

    public AbstractCriteriaQueryBuilder(EntityManager entityManager, Class<T> theClass){
        this.entityManager = entityManager;
        this.theClass = theClass;
    }

    public List<T> getResultList(BaseSearchParams searchParams){
        this.searchParams = searchParams;

        buildQuery();
        initiateListQuery();
        initiateCountQuery();
        resetResources();

        return resultList;
    }

    public Long getResultCount() {
        return resultCount;
    }

    private void buildQuery(){
        setCriteriaBuilder();
        setCriteriaQuery();
        setRoot();
        setPredicates();
        setOrderBy();
        setQueryConditionAndOrderBy();
    }

    private void setCriteriaBuilder(){
        if(isNull(builder))
            builder = entityManager.getCriteriaBuilder();
    }

        private void setCriteriaQuery(){
        if(isNull(query))
            query = builder.createQuery(theClass);
    }

    private void setQueryConditionAndOrderBy() {
        query
            .where(predicates)
            .orderBy(builder.desc(root.get(orderBy)));
    }

    private void initiateCountQuery(){
        CriteriaQuery<Long> countQuery = entityManager.getCriteriaBuilder().createQuery(Long.class);
        Root<T> root = countQuery.from(theClass);
        countQuery.select(  builder.count( root ) ).where(predicates);
        resultCount =  entityManager.createQuery(countQuery).getSingleResult();
    }

    private void resetResources(){
        builder = null;
        root = null;
        query = null;
        predicates = new Predicate[50];
    }

    abstract void setRoot();
    abstract void setPredicates();
    abstract void setOrderBy();
    abstract void initiateListQuery();
}