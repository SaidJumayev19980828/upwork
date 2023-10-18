package com.nasnav.commons.criteria;

import com.nasnav.persistence.ApiLogsEntity;
import com.nasnav.request.ApiLogsSearchParam;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.criteria.JoinType.LEFT;

@Component("apiLogsQueryBuilder")
public class ApiLogsCriteriaQueryBuilder extends AbstractCriteriaQueryBuilder<ApiLogsEntity, ApiLogsSearchParam> {

	public ApiLogsCriteriaQueryBuilder(EntityManager entityManager) {
		super(entityManager, ApiLogsEntity.class);
	}

	@Override
	Root<ApiLogsEntity> getRoot(CriteriaQuery<ApiLogsEntity> query) {
		Root<ApiLogsEntity> root = query.from(ApiLogsEntity.class);
		root.fetch("loggedCustomer", LEFT);
		root.fetch("loggedEmployee", LEFT);
		root.fetch("organization", LEFT);

		return root;
	}

	@Override
	Predicate[] getPredicates(CriteriaBuilder builder, Root<ApiLogsEntity> root, ApiLogsSearchParam searchParam) {
		List<Predicate> predicatesList = new ArrayList<>();


		if(searchParam.getEmployees() != null) {
			if (searchParam.getEmployees()) {
				if (containUsersIds(searchParam)) {
					predicatesList.add(root.get("loggedEmployee").in(searchParam.getUsers()));
				} else {
					predicatesList.add(root.get("loggedEmployee").isNotNull());
				}
			} else {
				if (containUsersIds(searchParam)) {
					predicatesList.add(root.get("loggedCustomer").in(searchParam.getUsers()));
				} else {
					predicatesList.add(root.get("loggedCustomer").isNotNull());
				}
			}
		}
		if (searchParam.getOrganizations() != null){
			predicatesList.add(root.get("organization").in(searchParam.getOrganizations()));
		}
		if(searchParam.getCreated_after() != null) {
			predicatesList.add( builder.greaterThanOrEqualTo( root.<LocalDateTime>get("callDate"), builder.literal(readDate(searchParam.getCreated_after()))) );

		}
		if(searchParam.getCreated_before() != null) {
			predicatesList.add( builder.lessThanOrEqualTo( root.<LocalDateTime>get("callDate"), builder.literal(readDate(searchParam.getCreated_before()))) );

		}

		return predicatesList.stream().toArray(Predicate[]::new);
	}

	private boolean containUsersIds(ApiLogsSearchParam searchParam){
		return searchParam.getUsers() != null && ! searchParam.getUsers().isEmpty();
	}

	private LocalDateTime readDate(String dateStr) {
		return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
	}

	@Override
	void updateQueryWithConditionAndOrderBy(CriteriaQueryContext<ApiLogsEntity, ApiLogsSearchParam> context) {
		context.getQuery()
				.where(context.getPredicates())
				.orderBy(context.getCriteriaBuilder()
				.desc( context.getRoot().get("callDate") ));
	}

	@Override
	List<ApiLogsEntity> queryForList(CriteriaQuery<ApiLogsEntity> query, ApiLogsSearchParam params) {
		return entityManager.createQuery(query)
				.setFirstResult(params.getStart())
				.setMaxResults(params.getCount())
				.getResultList();
	}
}
