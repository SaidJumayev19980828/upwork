package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.StocksEntity;

public interface StockRepository extends CrudRepository<StocksEntity, Long> {

	List<StocksEntity> findByShopsEntity_IdAndProductVariantsEntity_Id( Long shopId, Long variantId);


	@Query("select stock from StocksEntity stock "
			+ " join stock.productVariantsEntity var "
			+ " join var.productEntity prod "
			+ " where prod.id= :productId  "
			+ " and stock.shopsEntity.id = :shopsId "
			+ " order by prod.id"
			)
	List<StocksEntity> findByProductIdAndShopsId(@Param("productId") Long productId, @Param("shopsId") Long shopsId);


	List<StocksEntity> findByProductVariantsEntityIdAndShopsEntityIdOrderByPriceAsc(Long variantId, Long shopId);


	List<StocksEntity> findByProductVariantsEntityIdOrderByPriceAsc(Long variantId);


	List<StocksEntity> findByShopsEntity_Id(Long id);


	@Query("select stock from StocksEntity stock "
			+ " left join stock.productVariantsEntity var "
			+ " left join var.productEntity prod "
			+ " where prod.id in :productIds  " )
	List<StocksEntity> findByProductIdIn(@Param("productIds") List<Long> productIds);


	StocksEntity getOne(Long stockId);


	Long countByProductVariantsEntity_Id(Long testVariantId);


	Optional<StocksEntity> findByProductVariantsEntity_IdAndShopsEntity_Id(Long variantId, Long shopId);


	List<StocksEntity> findByProductVariantsEntity_Id(Long id);
}
