package com.nasnav.commons.criteria;

import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.request.UsersSearchParam;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static javax.persistence.criteria.JoinType.LEFT;

@Component("userListQueryBuilder")
public class UserListCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<EmployeeUserEntity, UsersSearchParam> {
    public UserListCriteriaQueryBuilder(EntityManager entityManager) {
        super(entityManager, EmployeeUserEntity.class);
    }

    @Override
    Root<EmployeeUserEntity> getRoot(CriteriaQuery<EmployeeUserEntity> query) {
        Root<EmployeeUserEntity> root = query.distinct(true).from(EmployeeUserEntity.class);
        root.fetch("roles", LEFT);

        return root;
    }

    @Override
    Predicate[] getPredicates(CriteriaBuilder builder, Root<EmployeeUserEntity> root, UsersSearchParam params) {

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
        return predicatesList.stream().toArray(Predicate[]::new);
    }

        @Override
        void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<EmployeeUserEntity, UsersSearchParam> context) {
            context.getQuery().where(context.getPredicates());

            // TODO: Order by id
        }

    @Override
    List<EmployeeUserEntity> queryForList(CriteriaQuery<EmployeeUserEntity> query, UsersSearchParam params) {
        return entityManager.createQuery(query)
                .getResultList();
    }
}
