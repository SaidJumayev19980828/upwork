package com.nasnav.dao;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.StocksEntity;

import java.util.List;

public interface StockRepository extends CrudRepository<StocksEntity, Long> {

	List<StocksEntity> findByProductEntity_IdAndShopsEntity_IdAndProductVariantsEntity_Id(Long productId, Long shopId,
	                                                                                      Long variantId);

	List<StocksEntity> findByProductEntity_IdAndShopsEntity_Id(Long productId, Long shopId);

	List<StocksEntity> findByShopsEntity_Id(Long id);

	List<StocksEntity> findByProductEntity_IdIn(List<Long> productIds);

	void deleteByProductEntity_Id(Long productId);
}
