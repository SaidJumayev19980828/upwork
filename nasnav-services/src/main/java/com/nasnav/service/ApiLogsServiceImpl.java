package com.nasnav.service;

import com.nasnav.commons.criteria.AbstractCriteriaQueryBuilder;
import com.nasnav.dto.response.ApiLogsDTO;
import com.nasnav.dto.response.ApiLogsResponse;
import com.nasnav.persistence.ApiLogsEntity;
import com.nasnav.request.ApiLogsSearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiLogsServiceImpl implements ApiLogsService{
	private static final int API_LOGS_DEFAULT_COUNT = 1000;

	@Autowired
	@Qualifier("apiLogsQueryBuilder")
	private AbstractCriteriaQueryBuilder<ApiLogsEntity> criteriaQueryBuilder;


	@Override
	public ApiLogsResponse getAPIsCalls(ApiLogsSearchParam searchParam) {
		setSearchStartAndCount(searchParam);

		List<ApiLogsEntity> resultList = criteriaQueryBuilder.getResultList(searchParam);
		List<ApiLogsDTO> resultDTOs = convertEntities(resultList);

		return getApiLogsResponse(resultDTOs);
	}

	private void setSearchStartAndCount(ApiLogsSearchParam params) {
		if (params.getStart() == null || params.getStart() < 0){
			params.setStart(0);
		}

		if (params.getCount() == null || params.getCount() <= 0 || params.getCount() >= API_LOGS_DEFAULT_COUNT){
			params.setCount(API_LOGS_DEFAULT_COUNT);
		}
	}

	private List<ApiLogsDTO> convertEntities(List<ApiLogsEntity> entities){
		return entities.stream()
					.map(ApiLogsEntity::getRepresentation)
					.map(ApiLogsDTO.class::cast)
					.collect(Collectors.toList());
	}

	private ApiLogsResponse getApiLogsResponse(List<ApiLogsDTO> dtos){
		return new ApiLogsResponse(criteriaQueryBuilder.getResultCount(), dtos);
	}
}