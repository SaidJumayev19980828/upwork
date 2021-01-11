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
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemData("
			+ " item.id, user.id, product.id, variant.id, variant.name, stock.id, variant.featureSpec, variant.weight"
			+ " , item.coverImage, stock.price, item.quantity"
			+ " , brand.id, brand.name, brand.logo, product.name, product.productType, stock.discount"
			+ " , item.additionalData) "
			+ " FROM WishlistItemEntity item "
			+ "	LEFT JOIN item.user user"			
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN BrandsEntity brand on product.brandId = brand.id "
			+ " WHERE user.id = :user_id")
	List<CartItemData> findCurrentCartItemsByUser_Id(@Param("user_id") Long userId);


	CartItemEntity findByIdAndUser_Id(Long id, Long userId);

	@Query("select stock.id from WishlistItemEntity item" +
			" LEFT JOIN item.stock stock " +
			" LEFT JOIN item.user user"+
			" where item.id = :itemId and user.id = :userId")
	Long findWishlistItemStockId(@Param("itemId") Long itemId,
								 @Param("userId") Long userId);

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
}
