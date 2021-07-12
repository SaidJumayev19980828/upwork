package com.nasnav.dao;

import java.math.BigDecimal;
import java.util.List;

import com.nasnav.persistence.dto.query.result.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.CartItemEntity;

public interface  CartItemRepository extends JpaRepository<CartItemEntity, Long> {
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemData("
			+ " item.id, user.id, product.id, variant.id, variant.name, stock.id, variant.featureSpec, variant.weight "
			+ " , item.coverImage, stock.price, item.quantity"
			+ " , brand.id, brand.name, brand.logo, product.name, product.productType, stock.discount"
			+ " , item.additionalData, unit.name) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"			
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.unit unit "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN BrandsEntity brand on product.brandId = brand.id "
			+ " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0")
	List<CartItemData> findCurrentCartItemsByUser_Id(@Param("user_id") Long userId);

	@Query("SELECT new com.nasnav.persistence.dto.query.result.CartStatisticsData(" +
			"variant.id, variant.name, variant.barcode, variant.productCode, variant.sku, sum(item.quantity), count (user.id)) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " WHERE product.organizationId = :orgId and product.removed = 0 and variant.removed = 0 "
			+ " group by variant.id, variant.id, variant.name, variant.productCode, variant.sku "
			+ " order by sum(item.quantity) desc , count (user.id) desc ")
	List<CartStatisticsData> findCartVariantsByOrg_Id(@Param("orgId") Long orgId, Pageable pageable);

	@Query("SELECT item "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " WHERE product.organizationId = :orgId and product.removed = 0 and variant.removed = 0 "
			+ " order by item.createdAt desc ")
	List<CartItemEntity> findUsersCartsOrg_Id(@Param("orgId") Long orgId);

	CartItemEntity findByIdAndUser_Id(Long id, Long userId);
	CartItemEntity findByStock_IdAndUser_Id(Long stockId, Long userId);

	@Transactional
	@Modifying
	void deleteByIdAndUser_Id(Long id, Long userId);

	@Transactional
	@Modifying
	void deleteByQuantityAndUser_Id(Integer quantity, Long userId);

	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart WHERE cart.id in" +
			" (SELECT item.id " +
			" FROM CartItemEntity item " +
			" LEFT JOIN item.stock stock" +
			" LEFT JOIN stock.organizationEntity org" +
			" WHERE org.id = :orgId)")
	void deleteByOrganizationId(@Param("orgId")Long orgId);

	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart WHERE cart.id in" +
			" (SELECT item.id " +
			" FROM CartItemEntity item " +
			" LEFT JOIN item.stock stock" +
			" LEFT JOIN stock.productVariantsEntity variant" +
			" LEFT JOIN variant.productEntity product" +
			" WHERE product.id in :productIds)")
	void deleteByProductIdIn(@Param("productIds") List<Long> productIds);

	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart WHERE cart.id in" +
			" (SELECT item.id " +
			" FROM CartItemEntity item " +
			" LEFT JOIN item.stock stock" +
			" LEFT JOIN stock.productVariantsEntity variant" +
			" WHERE variant.id in :variantIds)")
	void deleteByVariantIdIn(@Param("variantIds") List<Long> variantIds);
	
	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart "
			+ " WHERE cart.id in ( "
			+ " SELECT item.id "
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.user usr "
			+ " WHERE usr.id = :user_id"
			+ ")")
	void deleteByUser_Id(@Param("user_id")Long userId);
	
	
	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity item "
			+ " WHERE item.id in ( "
			+ " SELECT itm.id FROM CartItemEntity itm "
			+ " LEFT JOIN itm.stock stock "
			+ " WHERE stock.id in :ids"
			+ ")")
	void deleteByStock_IdIn(@Param("ids")List<Long> ids);

	Long countByUser_Id(Long userId);
	
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemShippingData( "
			+ " stock.id, shop.id, addr.id, stock.price, stock.discount, item.quantity, variant.weight)"
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ "	LEFT JOIN item.user user "
			+ " LEFT JOIN stock.shopsEntity shop "
			+ " LEFT JOIN shop.addressesEntity addr"
			+ " WHERE user.id = :user_id")
	List<CartItemShippingData> findCartItemsShippingDataByUser_Id(@Param("user_id") Long userId);

	@Query("select sum( (COALESCE(stock.price, 0.0) - COALESCE(stock.discount, 0.0)) * COALESCE (item.quantity, 0) )"
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ "	LEFT JOIN item.user user "
			+ " WHERE user.id = :user_id")
	BigDecimal findTotalCartValueByUser_Id(@Param("user_id") Long userId);

	@Query("select count(stock.id)"
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ "	LEFT JOIN item.user user "
			+ " WHERE user.id = :user_id")
	Long findTotalCartQuantityByUser_Id(@Param("user_id") Long userId);

	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart "
			+ " WHERE cart.id in ( "
			+ " SELECT item.id "
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN item.user usr "
			+ " WHERE stock.id in :stock_ids"
			+ " AND usr.id = :user_id"
			+ ")")
	void deleteByStockIdInAndUser_Id(@Param("stock_ids")List<Long> stockIds, @Param("user_id")Long userId);

	
	
	@Query("SELECT new com.nasnav.persistence.dto.query.result.CartItemStock("
			+ " variant.id, allStocks.id, shop.id, city.id"
			+ ", allStocks.quantity, allStocks.price , allStocks.discount)"
			+ " FROM CartItemEntity item"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN item.user user "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.stocks  allStocks "
			+ " LEFT JOIN allStocks.shopsEntity shop "
			+ " LEFT JOIN shop.addressesEntity address "
			+ " LEFT JOIN address.areasEntity area "
			+ " LEFT JOIN area.citiesEntity city "
			+ " WHERE user.id = :userId "
			+ " AND allStocks.quantity >= item.quantity "
			+ " AND shop.removed = 0")
	List<CartItemStock> getAllCartStocks(@Param("userId") Long userId);

	
	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart "
			+ " WHERE cart.id in ( "
			+ " SELECT item.id "
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN item.user usr "
			+ " WHERE variant.id in :variant_ids"
			+ " AND usr.id = :user_id"
			+ ")")
	void deleteByVariantIdInAndUser_Id(@Param("variant_ids")List<Long> variantIds, @Param("user_id")Long userId);
}
