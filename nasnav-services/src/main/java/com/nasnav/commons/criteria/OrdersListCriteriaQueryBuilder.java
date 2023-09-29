package com.nasnav.commons.criteria;

import com.nasnav.enumerations.OrderSortOptions;
import com.nasnav.enumerations.SortingWay;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.request.OrderSearchParam;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.nasnav.commons.utils.EntityUtils.notNullNorEmpty;
import static com.nasnav.enumerations.OrderStatus.DISCARDED;
import static java.util.Optional.ofNullable;
import static javax.persistence.criteria.JoinType.LEFT;

@Component("ordersQueryBuilder")
public class OrdersListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<OrdersEntity, OrderSearchParam> {
    public OrdersListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, OrdersEntity.class);
    }

    @Override
    Root<OrdersEntity> getRoot(CriteriaQuery<OrdersEntity> query) {
        Root<OrdersEntity> root = query.from(OrdersEntity.class);
        root.fetch("metaOrder", LEFT);
        root.fetch("shipment", LEFT);
        root.fetch("addressEntity", LEFT)
                .fetch("areasEntity", LEFT)
                .fetch("citiesEntity", LEFT)
                .fetch("countriesEntity", LEFT);
        var basket = root.fetch("basketsEntity", LEFT);
        basket.fetch("addons", LEFT)
                .fetch("stocksEntity", LEFT);
        basket.fetch("stocksEntity", LEFT)
                .fetch("productVariantsEntity", LEFT)
                .fetch("productEntity", LEFT);
        root.fetch("organizationEntity", LEFT);
        root.fetch("paymentEntity", LEFT);
        root.fetch("gainedPointsTransaction", LEFT);

        return root;
    }

    @Override
    Predicate[] getPredicates(CriteriaBuilder builder, Root<OrdersEntity> root, OrderSearchParam params) {
        return buildOrderPredicates(builder, root, params);
    }

    public static Predicate [] buildOrderPredicates(CriteriaBuilder builder, Path<OrdersEntity> root, OrderSearchParam params) {
        List<Predicate> predicatesList = new ArrayList<>();
        predicatesList.add( builder.notEqual(root.get("status"), DISCARDED.getValue()) );
        if(params.getUser_id() != null)
            predicatesList.add( builder.equal(root.get("userId"), params.getUser_id()) );
        if(params.getOrg_id() != null) {
            predicatesList.add(root.get("organizationEntity").get("id").in(params.getOrg_id()));
        }
        if(params.getShop_id() != null) {
            predicatesList.add(root.get("shopsEntity").get("id").in(params.getShop_id()));
        }
        if(notNullNorEmpty(params.getStatus_ids()))
            predicatesList.add(root.get("status").in(params.getStatus_ids()));
        if(params.getUpdated_after() != null) {
            predicatesList.add( builder.greaterThanOrEqualTo( root.<LocalDateTime>get("updateDate"), builder.literal(readDate(params.getUpdated_after())) ) );
        }
        if(params.getUpdated_before() != null) {
            predicatesList.add( builder.lessThanOrEqualTo( root.<LocalDateTime>get("updateDate"), builder.literal(readDate(params.getUpdated_before()))) );
        }
        if(params.getCreated_after() != null) {
            predicatesList.add( builder.greaterThanOrEqualTo( root.<LocalDateTime>get("creationDate"), builder.literal(readDate(params.getCreated_after())) ) );
        }
        if(params.getCreated_before() != null) {
            predicatesList.add( builder.lessThanOrEqualTo( root.<LocalDateTime>get("creationDate"), builder.literal(readDate(params.getCreated_before()))) );
        }
        if(params.getShipping_service_id() != null){
            predicatesList.add( builder.equal(root.get("shipment").get("shippingServiceId"), params.getShipping_service_id()) );
        }
        if(params.getPayment_operator() != null){
            predicatesList.add( builder.equal(root.get("paymentEntity").get("operator"), params.getPayment_operator()) );
        }
        if(params.getMin_total() != null) {
            predicatesList.add(builder.ge(root.get("total"), params.getMin_total()));
        }
        if(params.getMax_total() != null) {
            predicatesList.add(builder.le(root.get("total"), params.getMax_total()));
        }
        return predicatesList.stream().toArray(Predicate[]::new);
    }

    private String getOrderBy(OrderSearchParam params) {

        String orderColumn = ofNullable(params.getOrders_sorting_option())
                            .map(OrderSortOptions::getValue)
                            .orElse("updateDate");

        if(orderColumn.equalsIgnoreCase("quantity"))
            orderColumn = null;
        
        return orderColumn;
    }

    @Override
    void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<OrdersEntity, OrderSearchParam> context) {
        CriteriaQuery<OrdersEntity> query = context.getQuery();
        query.where(context.getPredicates());

        String orderColumn = getOrderBy(context.getSearchParams());
        if(orderColumn != null){
            query.orderBy(getSortingWay(context.getCriteriaBuilder(), context.getRoot(), context.getSearchParams(), orderColumn));
        }
    }

    private Order getSortingWay(CriteriaBuilder criteriaBuilder, Root<OrdersEntity> root, OrderSearchParam params,
            String orderColumn) {
        SortingWay sortingWay = params.getSorting_way();

        if(sortingWay.equals(SortingWay.ASC))
            return criteriaBuilder.asc( root.get(orderColumn) );
        else
            return criteriaBuilder.desc( root.get(orderColumn) );
    }

    @Override
    List<OrdersEntity> queryForList(CriteriaQuery<OrdersEntity> query, OrderSearchParam params) {
        Boolean useCount = ofNullable(params.getUseCount())
                .orElse(true);

        List<OrdersEntity> results = null;
        if(Boolean.TRUE.equals(useCount)){
            results = entityManager.createQuery(query)
                                    .setFirstResult(params.getStart())
                                    .setMaxResults(params.getCount())
                                    .getResultList();
        }else{
            results = entityManager.createQuery(query)
                                    .getResultList();
        }

        return results;
    }

    private static LocalDateTime readDate(String dateStr) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
    }
}