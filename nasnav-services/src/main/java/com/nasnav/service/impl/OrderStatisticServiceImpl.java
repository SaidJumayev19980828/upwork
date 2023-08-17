package com.nasnav.service.impl;

import com.nasnav.dto.Dates;
import com.nasnav.dto.Prices;
import com.nasnav.dto.Quantities;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.service.OrderStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigDecimal;

import static com.nasnav.commons.criteria.OrdersListCriteriaQueryBuilder.buildOrderPredicates;

@Service
public class OrderStatisticServiceImpl implements OrderStatisticService {
    @Autowired
    private EntityManager em;


    @Override
    public Dates getOrderDatesStatistic(OrderSearchParam finalParams) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Dates> cq = cb.createQuery(Dates.class);
        Root<OrdersEntity> root = cq.from(OrdersEntity.class);
        Predicate[] orderPredicates = buildOrderPredicates(cb, root, finalParams);


        CriteriaQuery<Dates> total = cq.multiselect(cb.min(root.get("creationDate")).alias("minCreatedDate"), cb.max(root.get("creationDate")).alias("maxCreatedDate")).where(orderPredicates);
        return em.createQuery(total).getSingleResult();
    }

    @Override
    public Prices getOrderPricesStatistic(OrderSearchParam finalParams) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Prices> cq = cb.createQuery(Prices.class);
        Root<OrdersEntity> root = cq.from(OrdersEntity.class);
        Predicate[] orderPredicates = buildOrderPredicates(cb, root, finalParams);


        CriteriaQuery<Prices> total = cq.multiselect(cb.min(root.get("total")).alias("minPrice"), cb.max(root.get("total")).alias("maxPrice")).where(orderPredicates);
        return em.createQuery(total).getSingleResult();
    }

    @Override
    public Quantities getOrderQuantitiesStatistic(OrderSearchParam finalParams) {
        return new Quantities(getQuantity(finalParams, true).intValue(), getQuantity(finalParams, false).intValue());
    }

    private BigDecimal getQuantity(OrderSearchParam finalParams, boolean direction) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<BasketsEntity> root = cq.from(BasketsEntity.class);

        Join<BasketsEntity, OrdersEntity> ordersEntity = root.join("ordersEntity", JoinType.INNER);
        Predicate[] basketPredicates = buildOrderPredicates(cb, ordersEntity, finalParams);

        Expression<BigDecimal> sumExpr = cb.sum(root.get("quantity"));
        CriteriaQuery<BigDecimal> quantityQuery = cq.select(sumExpr)
                .where(basketPredicates)
                .groupBy(ordersEntity)
                .orderBy(direction ? cb.asc(sumExpr) : cb.desc(sumExpr));

        return em.createQuery(quantityQuery).setMaxResults(1).getSingleResult();
    }
}
