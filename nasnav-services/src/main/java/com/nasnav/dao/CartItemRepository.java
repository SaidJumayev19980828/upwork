package com.nasnav.dao;

import com.nasnav.dto.Pair;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

public interface  CartItemRepository extends JpaRepository<CartItemEntity, Long> {
	@Query("SELECT distinct item "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN FETCH item.user user"
			+ " LEFT JOIN FETCH item.stock stock "
			+ " LEFT JOIN FETCH stock.unit unit "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.featureValues featureValues"
			+ " LEFT JOIN FETCH featureValues.feature feature "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " LEFT JOIN FETCH product.brand brand "
			+ " LEFT JOIN FETCH item.addons addons "
			+ " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0")
	List<CartItemEntity> findCurrentCartItemsByUser_Id(@Param("user_id") Long userId);

	@Query("SELECT distinct item FROM CartItemEntity item "
			+ "LEFT JOIN FETCH item.user user "
			+ "LEFT JOIN FETCH item.stock stock "
			+ "LEFT JOIN FETCH stock.unit unit "
			+ "LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ "LEFT JOIN FETCH variant.featureValues featureValues "
			+ "LEFT JOIN FETCH featureValues.feature feature "
			+ "LEFT JOIN FETCH variant.productEntity product "
			+ "LEFT JOIN FETCH product.brand brand "
			+ "LEFT JOIN FETCH item.addons addons "
			+ "WHERE user.id = :user_id and stock.id in :stockIds and product.removed = 0 and variant.removed = 0 ")
	List<CartItemEntity> findCurrentCartSelectedItemsByUserId(@Param("user_id") Long userId, Set<Long> stockIds);
	@Query("SELECT distinct item "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN FETCH item.user user"
			+ " LEFT JOIN FETCH item.stock stock "
			+ " LEFT JOIN FETCH stock.unit unit "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.featureValues featureValues"
			+ " LEFT JOIN FETCH featureValues.feature feature "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " LEFT JOIN FETCH product.brand brand "
			+ " LEFT JOIN FETCH item.addons addons "
			+ " WHERE user.id = :user_id and stock.organizationEntity.id = :organization_id and product.removed = 0 and variant.removed = 0")
	List<CartItemEntity> findCurrentCartItemsByUserIdAndOrgId(@Param("user_id") Long userId,@Param("organization_id") Long OrgId);

	@Query("SELECT distinct new com.nasnav.persistence.dto.query.result.CartStatisticsData(" +
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

	@Query("SELECT distinct item "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " WHERE product.organizationId = :orgId and product.removed = 0 and variant.removed = 0 "
			+ " and user.userStatus = 201 "
			+ " order by item.createdAt desc ")
	List<CartItemEntity> findUsersCartsOrg_Id(@Param("orgId") Long orgId);

	@Query("SELECT distinct item "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " WHERE product.organizationId = :orgId and user.id in :userIds"
			+ " and product.removed = 0 and variant.removed = 0  and user.userStatus = 201 "
			+ " order by item.createdAt desc ")
	List<CartItemEntity> findCartsByUsersIdAndOrg_Id(@Param("userIds")List<Long> userIds,
													 @Param("orgId") Long orgId);

	CartItemEntity findByIdAndUser_Id(Long id, Long userId);
	CartItemEntity findByStock_IdAndUser_Id(Long stockId, Long userId);

	@Query("select c from CartItemEntity c" +
			" left join fetch c.stock s" +
			" left join fetch s.productVariantsEntity v" +
			" left join fetch v.productEntity p" +
			" where s.quantity = 0 and c.quantity > 0")
	List<CartItemEntity> findOutOfStockCartItems();

	@Query("select c from CartItemEntity c" +
			" left join fetch c.stock s" +
			" left join fetch c.user u" +
			" left join fetch s.productVariantsEntity v" +
			" left join fetch v.productEntity p" +
			" where s.quantity = 0 and c.quantity > 0 and u.id = :userId" +
			" order by s.price, s.discount")
	List<CartItemEntity> findUserOutOfStockCartItems(@Param("userId") Long userId);

	@Query("select c from CartItemEntity c" +
			" left join fetch c.stock s" +
			" left join fetch c.user u" +
			" left join fetch s.productVariantsEntity v" +
			" left join fetch v.productEntity p" +
			" where s.quantity = 0 and s.id in :stockIds and c.quantity > 0 and u.id = :userId" +
			" order by s.price, s.discount")
	List<CartItemEntity> findUserOutOfStockSelectedCartItems(@Param("userId") Long userId, Set<Long> stockIds);

	@Transactional
	@Modifying
	void deleteByIdAndUser_Id(Long id, Long userId);

	@Transactional
	@Modifying
	@Query("DELETE FROM CartItemEntity cart " +
			"WHERE cart.id in :cartItemsIds" )
	void deleteByCartItemId(@Param("cartItemsIds") Set<Long> cartItemsIds);

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

	Long countByStock_ShopsEntity_Id(Long shopId);
	
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
			+ " shop.organizationEntity.id, variant.id, allStocks.id, shop.id, city.id"
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

	@Query("SELECT new com.nasnav.persistence.dto.query.result.CartItemStock("
			+ " shop.organizationEntity.id, variant.id, allStocks.id, shop.id, city.id"
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
			+ " AND stock.id in :stockIds "
			+ " AND shop.removed = 0")
	List<CartItemStock> getAllCartSelectedStocks(@Param("userId") Long userId, Set<Long> stockIds);

	@Query("SELECT new com.nasnav.persistence.dto.query.result.CartItemStock("
			+ " shop.organizationEntity.id, variant.id, allStocks.id, shop.id, city.id"
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
			+ " AND shop.removed = 0 AND shop.id in :shopIds")
	List<CartItemStock> getAllCartStocks(@Param("userId") Long userId, @Param("shopIds") List<Long> shopIds);

	@Query("SELECT variant "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0")
	List<ProductVariantsEntity> findCurrentCartVariantsByUser_Id(@Param("user_id") Long userId);

	@Query("SELECT new com.nasnav.dto.Pair(shop.id, variant.id) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.stocks allStocks "
			+ " LEFT JOIN allStocks.shopsEntity shop "
			+ " LEFT JOIN variant.productEntity product "
			+ " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0 and shop.removed = 0 and allStocks.quantity >= item.quantity")
	List<Pair> findCartVariantAndShopPairByUser_Id(@Param("user_id") Long userId);
	
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

	@Transactional
	@Modifying
	@Query(value = "update cart_items set is_wishlist = 1, quantity = null where id in :ids", nativeQuery = true)
	void moveCartItemsToWishlistItems(@Param("ids") Set<Long> ids);


	long countByUser_IdAndStock_ShopsEntity_IdNot(Long userId, Long storeId);

	long countByUserIdAndStockShopsEntityIdNotAndStockIdIn(Long userId, Long storeId, Set<Long> stockIds);
}
