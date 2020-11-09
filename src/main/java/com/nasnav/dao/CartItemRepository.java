package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.persistence.dto.query.result.CartItemData;
import com.nasnav.persistence.dto.query.result.CartItemShippingData;
import com.nasnav.persistence.dto.query.result.CartItemStock;

public interface  CartItemRepository extends JpaRepository<CartItemEntity, Long> {
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemData("
			+ " item.id, user.id, product.id, variant.id, variant.name, stock.id, variant.featureSpec "
			+ " , item.coverImage, stock.price, item.quantity"
			+ " , brand.id, brand.name, brand.logo, product.name, product.productType, stock.discount"
			+"  , item.additionalData) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"			
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN BrandsEntity brand on product.brandId = brand.id "
			+ " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0")
	List<CartItemData> findCurrentCartItemsByUser_Id(@Param("user_id") Long userId);

	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemData("
			+ " item.id, user.id, product.id, variant.id, variant.name, stock.id, variant.featureSpec "
			+ " , item.coverImage, stock.price, item.quantity"
			+ " , brand.id, brand.name, brand.logo, product.name, product.productType, stock.discount"
			+"  , item.additionalData) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN BrandsEntity brand on product.brandId = brand.id "
			+ " WHERE product.organizationId = :orgId and product.removed = 0 and variant.removed = 0")
	List<CartItemData> findCartsByOrg_Id(@Param("orgId") Long orgId);

	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartCheckoutData("
			+ " item.id, stock.id, stock.currency, stock.price, item.quantity,"
			+ " variant.barcode,  product.name, variant.featureSpec, shop.id, address"
			+ ", product.organizationId, stock.discount) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN stock.shopsEntity shop "
			+ " LEFT JOIN shop.addressesEntity address"
			+ " WHERE user.id = :user_id AND product.hide = false and product.removed = 0 And variant.removed = 0")
	List<CartCheckoutData> getCheckoutCartByUser_Id(@Param("user_id") Long userId);

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
			+ " stock.id, shop.id, addr.id, stock.price, stock.discount, item.quantity)"
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ "	LEFT JOIN item.user user "
			+ " LEFT JOIN stock.shopsEntity shop "
			+ " LEFT JOIN shop.addressesEntity addr"
			+ " WHERE user.id = :user_id")
	List<CartItemShippingData> findCartItemsShippingDataByUser_Id(@Param("user_id") Long userId);
	
	

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
