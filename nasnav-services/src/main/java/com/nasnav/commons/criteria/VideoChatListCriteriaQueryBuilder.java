package com.nasnav.commons.criteria;

import com.nasnav.persistence.VideoChatLogEntity;
import com.nasnav.request.VideoChatSearchParam;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static javax.persistence.criteria.JoinType.LEFT;

@Component("videoChatQueryBuilder")

public class VideoChatListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<VideoChatLogEntity, VideoChatSearchParam>{
    private static final String ID = "id";
    private static final String USER = "user";
    private static final String ASSIGNED_TO = "assignedTo";
    private static final String ORGANIZATION = "organization";
    private static final String SHOP = "shop";
    private static final String CREATED_AT = "createdAt";
    private static final String ENDED_AT = "endedAt";
    private static final String STATUS = "status";
    private static final String IS_ACTIVE = "isActive";
    private Root<VideoChatLogEntity> root;

    public VideoChatListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, VideoChatLogEntity.class);
    }

    @Override
    Root<VideoChatLogEntity> getRoot(CriteriaQuery<VideoChatLogEntity> query) {
        root = query.distinct(true).from(VideoChatLogEntity.class);
        root.fetch(USER, LEFT);
        root.fetch(ASSIGNED_TO, LEFT);
        root.fetch(ORGANIZATION, LEFT);
        root.fetch(SHOP, LEFT);

        return root;
    }

    @Override
    Predicate[] getPredicates(CriteriaBuilder builder, Root<VideoChatLogEntity> root, VideoChatSearchParam searchParam) {
        List<Predicate> predicatesList = new ArrayList<>();

        if (searchParam.getOrgId() != null) {
            predicatesList.add( builder.equal( root.get(ORGANIZATION).get(ID), searchParam.getOrgId()));
        }
        if (searchParam.getShopId() != null) {
            predicatesList.add( builder.equal( root.get(SHOP).get(ID), searchParam.getShopId()));
        }
        if (searchParam.getHasShop() != null) {
            Function<Expression<?>, Predicate> validation = Boolean.TRUE.equals(searchParam.getHasShop())
                    ? builder::isNotNull
                    : builder::isNull;

            predicatesList.add( validation.apply( root.get(SHOP)));
        }
        if (searchParam.getIsActive() != null) {
            predicatesList.add( builder.isTrue( root.get(IS_ACTIVE)));
        }
        if (searchParam.getIsAssigned() != null) {
            predicatesList.add( builder.isNotNull( root.get(ASSIGNED_TO)));
        }
        if (searchParam.getEmployeeId() != null) {
            predicatesList.add( builder.equal( root.get(ASSIGNED_TO).get(ID), searchParam.getEmployeeId()));
        }
        if (searchParam.getUserId() != null) {
            predicatesList.add( builder.equal( root.get(USER).get(ID), searchParam.getUserId()));
        }
        if (searchParam.getStatus() != null) {
            Integer status = searchParam.getStatus().getValue();
            predicatesList.add( builder.equal( root.get(STATUS), status));
        }
        if (searchParam.getFrom() != null) {
            Predicate nullEndedAt = builder.isNull(root.get(ENDED_AT));
            Predicate endedLater = builder.greaterThan(root.get(ENDED_AT), searchParam.getFrom());
            predicatesList.add( builder.or(nullEndedAt, endedLater));
        }
        if (searchParam.getTo() != null) {
            Predicate nullCreatedAt = builder.isNull(root.get(CREATED_AT));
            Predicate startedEarlier = builder.lessThan(root.get(CREATED_AT), searchParam.getTo());
            predicatesList.add( builder.or(nullCreatedAt, startedEarlier));
        }

        return predicatesList.stream().toArray(Predicate[]::new);
    }

    @Override
    void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<VideoChatLogEntity, VideoChatSearchParam> context) {
        context.getQuery().where(context.getPredicates())
                .orderBy(context.getCriteriaBuilder().desc( root.get(ID)));
    }

    @Override
    List<VideoChatLogEntity> queryForList(CriteriaQuery<VideoChatLogEntity> query, VideoChatSearchParam params) {
        return entityManager.createQuery(query)
                .setFirstResult(params.getStart())
                .setMaxResults(params.getCount())
                .getResultList();
    }
}
