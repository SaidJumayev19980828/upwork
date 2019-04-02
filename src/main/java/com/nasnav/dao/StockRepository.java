package com.nasnav.dao;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.StocksEntity;

public interface StockRepository extends CrudRepository<StocksEntity, Long>{

	
}
