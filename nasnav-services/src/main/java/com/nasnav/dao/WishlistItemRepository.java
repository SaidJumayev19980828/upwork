package com.nasnav.dao;

import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.WishlistItemEntity;
import com.nasnav.persistence.dto.query.result.CartItemData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, Long> {

	@Query("SELECT distinct item "
			+ " FROM WishlistItemEntity item "
			+ "	LEFT JOIN FETCH item.user user"
			+ " LEFT JOIN FETCH item.stock stock "
			+ " LEFT JOIN FETCH stock.unit unit "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.featureValues featureValues"
			+ " LEFT JOIN FETCH featureValues.feature feature "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " LEFT JOIN FETCH product.brand brand "
			+ " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0")
	List<WishlistItemEntity> findCurrentCartItemsByUser_Id(@Param("user_id") Long userId);


	CartItemEntity findByIdAndUser_Id(Long id, Long userId);

	Long countByStock_ShopsEntity_Id(Long shopId);

	@Query("select stock.id from WishlistItemEntity item" +
			" LEFT JOIN item.stock stock " +
			" LEFT JOIN item.user user"+
			" where item.id = :itemId and user.id = :userId")
	Long findWishlistItemStockId(@Param("itemId") Long itemId,
								 @Param("userId") Long userId);

	@Query("SELECT distinct item "
			+ " FROM WishlistItemEntity item "
			+ "	LEFT JOIN fetch item.user user"
			+ " LEFT JOIN fetch item.stock stock "
			+ " LEFT JOIN fetch stock.productVariantsEntity variant "
			+ " LEFT JOIN fetch variant.featureValues featureValue "
			+ " LEFT JOIN fetch featureValue.feature feature "
			+ " LEFT JOIN fetch variant.productEntity product "
			+ " WHERE stock.quantity is not null and stock.quantity > 0 and "
			+ " product.removed = 0 and variant.removed = 0 and user.userStatus = 201 "
			+ " and item.additionalData like '%out_of_stock%'")
	List<WishlistItemEntity> findUsersWishListsWithZeroStockQuantity();

	@Transactional
	@Modifying
	void deleteByIdAndUser_Id(Long id, Long userId);

	WishlistItemEntity findByStock_IdAndUser_Id(Long id, Long id1);


	@Transactional
	@Modifying
	@Query(value =
			"update CART_ITEMS " +
			" set is_wishlist = 0 " +
			" , quantity = :qty " +
			" WHERE id = :id " +
			" AND user_id = :userId " +
			" AND is_wishlist = 1 "
			, nativeQuery = true)
	void moveToCart(@Param("id")Long id, @Param("qty") Integer qty, @Param("userId") Long userId);

	boolean existsByIdAndUser_Id(Long itemId, Long id);

	@Query(value = "SELECT item.stock.id FROM WishlistItemEntity item WHERE item.user.id = :userId")
	List<Long> getAllWishlistStocks(@Param("userId") Long userId);
}
