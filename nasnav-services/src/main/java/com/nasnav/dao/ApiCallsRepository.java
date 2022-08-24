package com.nasnav.dao;

import com.nasnav.persistence.ApiLogsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiCallsRepository extends CrudRepository<ApiLogsEntity, Long> {
	public List<ApiLogsEntity> findAll();
}