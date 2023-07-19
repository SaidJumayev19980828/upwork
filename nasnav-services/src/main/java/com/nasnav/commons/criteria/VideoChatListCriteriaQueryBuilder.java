package com.nasnav.commons.criteria;

import com.nasnav.persistence.VideoChatLogEntity;
import com.nasnav.request.VideoChatSearchParam;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.criteria.JoinType.LEFT;

@Component("videoChatQueryBuilder")

public class VideoChatListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<VideoChatLogEntity>{

    private Root<VideoChatLogEntity> root;

    public VideoChatListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, VideoChatLogEntity.class);
    }

    @Override
    void setRoot() {
        root = query.distinct(true).from(VideoChatLogEntity.class);
        root.fetch("user", LEFT);
        root.fetch("assignedTo", LEFT);
        root.fetch("organization", LEFT);
        root.fetch("shop", LEFT);
    }

    @Override
    void setPredicates() {
        VideoChatSearchParam searchParam = (VideoChatSearchParam) this.searchParams;
        List<Predicate> predicatesList = new ArrayList<>();

        if (searchParam.getOrgId() != null) {
            predicatesList.add( builder.equal( root.get("organization").get("id"), searchParam.getOrgId()));
        }
        if (searchParam.getShopId() != null) {
            predicatesList.add( builder.equal( root.get("shop").get("id"), searchParam.getShopId()));
        }
        if (searchParam.getHasShop() != null) {
            predicatesList.add( builder.isNotNull( root.get("shop")));
        }
        if (searchParam.getIsActive() != null) {
            predicatesList.add( builder.isTrue( root.get("isActive")));
        }
        if (searchParam.getIsAssigned() != null) {
            predicatesList.add( builder.isNotNull( root.get("assignedTo")));
        }
        if (searchParam.getStatus() != null) {
            Integer status = searchParam.getStatus().getValue();
            predicatesList.add( builder.equal( root.get("status"), status));
        }

        this.predicates = predicatesList.stream().toArray(Predicate[]::new);
    }

    @Override
    void setOrderBy() {
        this.orderBy = "id";
    }

    @Override
    void setQueryConditionAndOrderBy() {
        query.where(predicates)
                .orderBy(builder.desc( root.get(orderBy) ));
    }

    @Override
    void initiateListQuery() {
        VideoChatSearchParam params = (VideoChatSearchParam) this.searchParams;
        this.resultList = entityManager.createQuery(query)
                .setFirstResult(params.getStart())
                .setMaxResults(params.getCount())
                .getResultList();
    }
}
