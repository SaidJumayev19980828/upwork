package com.nasnav.commons.criteria;

import com.nasnav.persistence.ProductThreeDModel;
import com.nasnav.request.ThreeDModelSearchParam;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Component("threeDModelCriteriaQueryBuilder")
public class ThreeDModelCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<ProductThreeDModel, ThreeDModelSearchParam> {

    public ThreeDModelCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, ProductThreeDModel.class);
    }

    @Override
    Root<ProductThreeDModel> getRoot(CriteriaQuery<ProductThreeDModel> query) {
        return query.from(ProductThreeDModel.class);
    }

    @Override
    Predicate[] getPredicates(CriteriaBuilder criteriaBuilder, Root<ProductThreeDModel> root, ThreeDModelSearchParam searchParams) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        if (searchParams.getName() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchParams.getName().toLowerCase() + "%"));
        }
        if (searchParams.getBarcode() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("barcode")), searchParams.getBarcode().toLowerCase() + "%"));
        }
        if (searchParams.getSku() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), searchParams.getSku().toLowerCase() + "%"));
        }
        if (searchParams.getDescription() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchParams.getDescription().toLowerCase() + "%"));
        }
        if (searchParams.getColor() != null) {
            predicates.add(criteriaBuilder.equal(root.get("color"), searchParams.getColor()));
        }
        if (searchParams.getSize() != null) {
            predicates.add(criteriaBuilder.equal(root.get("size"), searchParams.getSize()));
        }
        return predicates.toArray(Predicate[]::new);
    }

    @Override
    void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<ProductThreeDModel, ThreeDModelSearchParam> context) {
        context.getQuery()
                .where(context.getPredicates())
                .orderBy(context.getCriteriaBuilder().asc(context.getRoot().get("id")));
    }

    @Override
    List<ProductThreeDModel> queryForList(CriteriaQuery<ProductThreeDModel> query, ThreeDModelSearchParam searchParams) {
        return entityManager.createQuery(query)
                .setFirstResult(searchParams.getStart())
                .setMaxResults(searchParams.getCount())
                .getResultList();
    }
}
