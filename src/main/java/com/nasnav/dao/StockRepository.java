package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import com.nasnav.dto.Prices;
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


	@Query("select stock from StocksEntity stock "
			+ " left join stock.productVariantsEntity var "
			+ " left join stock.shopsEntity shop "
			+ " left join fetch stock.organizationEntity org"
			+ " where var.id in :variantIds  " )
	List<StocksEntity> findByProductVariantsEntity_IdIn(@Param("variantIds")List<Long> variantIds);
	
	
	
	@Query("select stock from BasketsEntity basket "
			+ " left join basket.stocksEntity stock "
			+ " where basket.ordersEntity.id = :orderId")
	List<StocksEntity> findItemStocksByOrderId(@Param("orderId") Long orderId);

	
		
	@Query("select product.productType from StocksEntity stock "
			+ " left join stock.productVariantsEntity variant "
			+ " left join variant.productEntity product "
			+ " where stock.id = :stockId")
	Optional<Integer> getStockProductType(@Param("stockId") Long id);


	@Query("select product.items from StocksEntity bundleStock"
			+ " left join bundleStock.productVariantsEntity variant "
			+ " left join variant.productEntity product"
//			+ " left join bundle.items itemsStocks"
			+ " where bundleStock.id = :stockId "
			+ " and TYPE(product) = BundleEntity")
	List<StocksEntity> findByBundleStockId(@Param("stockId")Long bundleStockId);


	@Query(value = "select NEW com.nasnav.dto.Prices(p.id, MIN(s.price) , MAX(s.price) )" +
			" from StocksEntity s join ProductVariantsEntity v on s.productVariantsEntity = v join ProductEntity p on v.productEntity = p" +
			" where p.id in :productIds group by p.id")
	List<Prices> getProductsPrices(@Param("productIds") List<Long> productIds);
}
