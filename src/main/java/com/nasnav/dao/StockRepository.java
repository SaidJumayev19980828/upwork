package com.nasnav.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.StocksEntity;

public interface StockRepository extends CrudRepository<StocksEntity, Long>{

	List<StocksEntity> findByShopsEntity_Id(Long id);
	
	List<StocksEntity> findByProductEntity_IdIn(List<Long> productIds);
}
