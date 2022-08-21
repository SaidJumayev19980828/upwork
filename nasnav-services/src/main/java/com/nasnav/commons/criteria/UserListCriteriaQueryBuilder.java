package com.nasnav.commons.criteria;

import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.request.UsersSearchParam;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static javax.persistence.criteria.JoinType.LEFT;

@Component("userListQueryBuilder")
public class UserListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<EmployeeUserEntity> {

    private Root<EmployeeUserEntity> root;

    public UserListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, EmployeeUserEntity.class);
    }

    @Override
    void setRoot() {
        root = query.distinct(true).from(EmployeeUserEntity.class);
        root.fetch("roles", LEFT);
    }

    @Override
    void setPredicates() {
        UsersSearchParam params = (UsersSearchParam) this.searchParams;

        List<Predicate> predicatesList = new ArrayList<>();

        if (params.getOrgId() != null) {
            predicatesList.add( builder.equal(root.get("organizationId"), params.getOrgId()) );
        }
        if (params.getShopId() != null) {
            predicatesList.add( builder.equal(root.get("shopId"), params.getShopId()) );
        }
        if (!isNullOrEmpty(params.getRoles())) {
            predicatesList.add( root.join("roles").get("name").in(params.getRoles()) );
        }
        this.predicates = predicatesList.stream().toArray(Predicate[]::new);
    }

    @Override
    void setOrderBy() {
        orderBy = "id";
    }

    @Override
    void initiateListQuery() {

        resultList = entityManager.createQuery(query)
                .getResultList();
    }

    @Override
    void setQueryConditionAndOrderBy() {
        query.where(predicates);
    }
}
