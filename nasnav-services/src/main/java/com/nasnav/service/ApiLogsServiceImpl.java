package com.nasnav.service;

import com.nasnav.commons.criteria.AbstractCriteriaQueryBuilder;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dto.response.ApiLogsDTO;
import com.nasnav.dto.response.ApiLogsResponse;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ApiLogsEntity;
import com.nasnav.request.ApiLogsSearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.DATE$TIME$0001;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class ApiLogsServiceImpl implements ApiLogsService{
	private static final int API_LOGS_DEFAULT_COUNT = 1000;

	@Autowired
	@Qualifier("apiLogsQueryBuilder")
	private AbstractCriteriaQueryBuilder<ApiLogsEntity> criteriaQueryBuilder;


	@Override
	public ApiLogsResponse getAPIsCalls(ApiLogsSearchParam searchParam) {
		validateDates(searchParam);
		setSearchStartAndCount(searchParam);

		List<ApiLogsDTO> resultList = criteriaQueryBuilder
				.getResultList(searchParam, true)
				.stream()
				.map(log -> (ApiLogsDTO) log.getRepresentation())
				.collect(Collectors.toList());

		return new ApiLogsResponse(criteriaQueryBuilder.getResultCount(), resultList);
	}

	private void validateDates(ApiLogsSearchParam searchParam) {
		if(!StringUtils.validDateTime(searchParam.getCreated_after()))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, DATE$TIME$0001, "created_after");

		if(!StringUtils.validDateTime(searchParam.getCreated_before()))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, DATE$TIME$0001, "created_before");
	}

	private void setSearchStartAndCount(ApiLogsSearchParam params) {
		if (params.getStart() == null || params.getStart() < 0) {
			params.setStart(0);
		}

		if (params.getCount() == null || params.getCount() <= 0 || params.getCount() >= API_LOGS_DEFAULT_COUNT) {
			params.setCount(API_LOGS_DEFAULT_COUNT);
		}
	}
}