package com.nasnav.commons.criteria;

import com.nasnav.enumerations.OrderSortOptions;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.request.OrderSearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.nasnav.commons.utils.EntityUtils.notNullNorEmpty;
import static com.nasnav.enumerations.OrderStatus.DISCARDED;
import static java.util.Optional.ofNullable;
import static javax.persistence.criteria.JoinType.LEFT;

@Component("ordersQueryBuilder")
public class OrdersListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder {

    @Autowired
    public OrdersListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, OrdersEntity.class);
    }

    @Override
    void setRoot() {
        root = query.from(OrdersEntity.class);
        root.fetch("metaOrder", LEFT);
        root.fetch("shipment", LEFT);
        root.fetch("addressEntity", LEFT)
                .fetch("areasEntity", LEFT)
                .fetch("citiesEntity", LEFT)
                .fetch("countriesEntity", LEFT);
        root.fetch("basketsEntity", LEFT)
                .fetch("stocksEntity", LEFT)
                .fetch("productVariantsEntity", LEFT)
                .fetch("productEntity", LEFT);
        root.fetch("organizationEntity", LEFT);
        root.fetch("paymentEntity", LEFT);
    }

    @Override
    void setPredicates() {
        OrderSearchParam params = (OrderSearchParam) this.searchParams;


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
            params.getStatus_ids()
                    .forEach(status -> {
                        predicatesList.add( builder.equal(root.get("status"), status) );
                    });
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
        this.predicates = predicatesList.stream().toArray(Predicate[]::new);
    }

    @Override
    void setOrderBy() {
        OrderSearchParam params = (OrderSearchParam) this.searchParams;

        this.orderBy = ofNullable(params.getOrders_sorting_option())
                            .map(OrderSortOptions::getValue)
                            .orElse("updateDate");
    }

    @Override
    void initiateListQuery() {
        OrderSearchParam params = (OrderSearchParam) searchParams;
        Boolean useCount = ofNullable(params.getUseCount())
                .orElse(true);

        if(useCount){
            this.resultList = entityManager.createQuery(query)
                                    .setFirstResult(params.getStart())
                                    .setMaxResults(params.getCount())
                                    .getResultList();
        }else{
            this.resultList = entityManager.createQuery(query)
                                    .setFirstResult(params.getStart())
                                    .getResultList();
        }
    }

    private LocalDateTime readDate(String dateStr) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
    }
}