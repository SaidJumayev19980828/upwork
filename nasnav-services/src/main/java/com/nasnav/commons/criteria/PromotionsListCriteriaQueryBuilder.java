package com.nasnav.commons.criteria;


import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.request.PromotionsSearchParams;
import com.nasnav.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.time.LocalDateTime.now;
import static javax.persistence.criteria.JoinType.INNER;

@Component("promotionQueryBuilder")
public class PromotionsListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder {

    @Autowired
    private SecurityService securityService;

    @Autowired
    public PromotionsListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, PromotionsEntity.class);
    }

    @Override
    void setRoot() {
        root = query.from(PromotionsEntity.class);
        root.fetch("organization", INNER);
        root.fetch("createdBy", INNER);
    }

    @Override
    void setPredicates() {
        PromotionsSearchParams params = (PromotionsSearchParams) this.searchParams;

        Long orgId = securityService.getCurrentUserOrganizationId();

        ArrayList<Predicate> predicates = new ArrayList<>();

        if(params.status.isPresent()) {
            Predicate predicate = builder.equal(root.get("status"), params.status.get());
            predicates.add(predicate);
        }

        if(params.id.isPresent()) {
            Predicate predicate = builder.equal(root.get("id"), params.id.get());
            predicates.add(predicate);
        }

        predicates.add(builder.equal(root.get("organization").get("id"), orgId));

        Predicate isPromotionInTimeWindowPredicate =
                createPromotionInTimeWindowPerdicate(builder, root, params);

        predicates.add(isPromotionInTimeWindowPredicate);

        this.predicates = predicates.stream().toArray(Predicate[]::new);
    }

    @Override
    void setOrderBy() {
        orderBy = "id";
    }

    @Override
    void initiateListQuery() {
        PromotionsSearchParams params = (PromotionsSearchParams) searchParams;

        this.resultList = entityManager.createQuery(query)
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
