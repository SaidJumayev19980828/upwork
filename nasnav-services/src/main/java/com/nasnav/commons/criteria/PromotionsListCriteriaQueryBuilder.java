package com.nasnav.commons.criteria;


import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.request.PromotionsSearchParams;
import com.nasnav.service.SecurityService;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static javax.persistence.criteria.JoinType.INNER;

@Component("promotionQueryBuilder")
public class PromotionsListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<PromotionsEntity, PromotionsSearchParams> {
    private final SecurityService securityService;

    public PromotionsListCriteriaQueryBuilder(EntityManager entityManager, SecurityService securityService) {
        super(entityManager, PromotionsEntity.class);
        this.securityService = securityService;
    }

    @Override
    Root<PromotionsEntity> getRoot(CriteriaQuery<PromotionsEntity> query) {
        Root<PromotionsEntity> root = query.from(PromotionsEntity.class);
        root.fetch("organization", INNER);
        root.fetch("createdBy", INNER);

        return root;
    }

    @Override
    Predicate[] getPredicates(CriteriaBuilder builder, Root<PromotionsEntity> root, PromotionsSearchParams params) {
        Long orgId = securityService.getCurrentUserOrganizationId();

        ArrayList<Predicate> predicates = new ArrayList<>();

        if (params.status.isPresent()) {
            Predicate predicate = builder.equal(root.get("status"), params.status.get());
            predicates.add(predicate);
        }

        if (params.id.isPresent()) {
            Predicate predicate = builder.equal(root.get("id"), params.id.get());
            predicates.add(predicate);
        }

        predicates.add(builder.equal(root.get("organization").get("id"), orgId));

        Predicate isPromotionInTimeWindowPredicate =
                createPromotionInTimeWindowPerdicate(builder, root, params);

        predicates.add(isPromotionInTimeWindowPredicate);

        return predicates.stream().toArray(Predicate[]::new);
    }

    @Override
    void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<PromotionsEntity, PromotionsSearchParams> context) {
        context.getQuery()
                .where(context.getPredicates())
                .orderBy(context.getCriteriaBuilder().desc(context.getRoot().get("id")));
    }

    @Override
    List<PromotionsEntity> queryForList(CriteriaQuery<PromotionsEntity> query, PromotionsSearchParams params) {
        return entityManager.createQuery(query)
                .setFirstResult(params.getStart())
                .setMaxResults(params.getCount())
                .getResultList();
    }

    private Predicate createPromotionInTimeWindowPerdicate(CriteriaBuilder builder, Root<PromotionsEntity> root,
                                                           PromotionsSearchParams searchParams) {
        LocalDateTime searchWindowStart = searchParams.startTime.orElse(now().minusYears(1000));
        LocalDateTime searchWindowEnd = searchParams.endTime.orElse(now().plusYears(1000));
        Path<LocalDateTime> promotionPeriodStart = root.get("dateStart");
        Path<LocalDateTime> promotionPeriodEnd = root.get("dateEnd");

        Predicate noIntersectionCondition1 =
                builder.lessThan(promotionPeriodEnd, searchWindowStart);
        Predicate noIntersectionCondition2 =
                builder.lessThan(builder.literal(searchWindowEnd), promotionPeriodStart);
        Predicate isPromotionInTimeWindowPredicate =
                builder.not(builder.or(noIntersectionCondition1, noIntersectionCondition2));
        return isPromotionInTimeWindowPredicate;
    }
}
