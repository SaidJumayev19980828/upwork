package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.dto.Prices;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.dto.query.result.StockAdditionalData;

import org.springframework.transaction.annotation.Transactional;

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


	@Query("SELECT stocks "
			+ " FROM StocksEntity stocks "
			+ " left join fetch stocks.shopsEntity shop "
			+ " left join fetch stocks.productVariantsEntity variant "
			+ " left join fetch variant.productEntity product "
			+ " WHERE shop.id = :id ")
	List<StocksEntity> findByShopsEntity_Id(@Param("id")Long id);


	@Query("SELECT stock from StocksEntity stock "
			+ " left join stock.productVariantsEntity var "
			+ " left join var.productEntity prod "
			+ " where prod.id in :productIds  " )
	List<StocksEntity> findByProductIdIn(@Param("productIds") List<Long> productIds);


	StocksEntity getOne(Long stockId);


	Long countByProductVariantsEntity_Id(Long testVariantId);


	Optional<StocksEntity> findByProductVariantsEntity_IdAndShopsEntity_Id(Long variantId, Long shopId);


	List<StocksEntity> findByProductVariantsEntity_Id(Long id);
	
	
	
	@Query("select stock from StocksEntity stock "
			+ " left join fetch stock.productVariantsEntity var "
			+ " left join stock.shopsEntity shop "
			+ " where var.id in :variantIds "
			+ " and shop.id = :shopId " )
	List<StocksEntity> findByProductVariantsEntity_IdInAndShopsEntity_Id(@Param("variantIds")List<Long> id, @Param("shopId")Long shopId);


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


	@Query(value = "select NEW com.nasnav.dto.Prices(p.id, MIN(s.price) , MAX(s.price) )" +
			" from ProductCollectionEntity p join p.variants v " +
			" join v.stocks s" +
			" where p.id in :productIds group by p.id")
	List<Prices> getCollectionsPrices(@Param("productIds") List<Long> productIds);
	
	
	@Query("SELECT stock FROM StocksEntity stock "
			+ " LEFT JOIN FETCH stock.shopsEntity shop "
			+ " WHERE stock.id = :id")
	Optional<StocksEntity> findWithAdditionalData(@Param("id")Long id);

	StocksEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);


	List<StocksEntity> findByIdInAndOrganizationEntity_Id(List<Long> itemStocks, Long orgId);
	
	
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.StockAdditionalData("
			+ " stock.id, stock.currency, "
			+ " variant.barcode,  product.name, variant.featureSpec, shop.id, address"
			+ ", product.organizationId, stock.discount) "
			+ " FROM StocksEntity stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " INNER JOIN variant.productEntity product "
			+ " LEFT JOIN stock.shopsEntity shop "
			+ " LEFT JOIN shop.addressesEntity address"
			+ " WHERE stock.id in :stockIds and product.removed = 0 And variant.removed = 0")
	List<StockAdditionalData> findAdditionalDataByStockIdIn(@Param("stockIds")List<Long> stockIds);

	@Transactional
	@Modifying
	@Query("DELETE FROM StocksEntity stock "
			+ " WHERE stock.shopsEntity.id = :shopId")
	void deleteByShopsEntity_Id(@Param("shopId") Long shopId);

	@Transactional
	@Modifying
	@Query(value = "update StocksEntity s set s.quantity = 0 where s.shopsEntity.id = :shopId")
	void setStocksQuantityZero(@Param("shopId") Long shopId);

	@Transactional
	@Modifying
	@Query(value = "update StocksEntity s set s.quantity = 0 where s.productVariantsEntity in (select v from ProductVariantsEntity v where v.productEntity.id = :productId)")
	void setProductStocksQuantityZero(@Param("productId") Long productId);

	Long countByShopsEntity_Id(long shopId);


}
