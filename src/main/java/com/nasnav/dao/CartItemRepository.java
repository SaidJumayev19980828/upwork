package com.nasnav.dao;

import java.util.List;

import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.dto.query.result.CartItemData;
import org.springframework.transaction.annotation.Transactional;
import com.nasnav.persistence.dto.query.result.CartItemShippingData;

public interface  CartItemRepository extends JpaRepository<CartItemEntity, Long> {
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemData("
			+ " item.id, user.id, product.id, variant.id, stock.id, variant.featureSpec "
			+ " , item.coverImage, stock.price, item.quantity"
			+ " , brand.id, brand.name, brand.logo ) "
			+ " FROM CartItemEntity item "
			+ "	LEFT JOIN item.user user"			
			+ " LEFT JOIN item.stock stock "
			+ " LEFT JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN BrandsEntity brand on product.brandId = brand.id "
			+ " WHERE user.id = :user_id")
	List<CartItemData> findCurrentCartItemsByUser_Id(@Param("user_id") Long userId);

	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartCheckoutData("
			+ " item.id, stock.id, stock.currency, stock.price, item.quantity,"
			+ " variant.barcode,  product.name, variant.featureSpec, shop.id, address) "
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
	void deleteByUser_Id(Long userId);

	Long countByUser_Id(Long userId);
	
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.CartItemShippingData( "
			+ " stock.id, shop.id, addr.id)"
			+ " FROM CartItemEntity item "
			+ " LEFT JOIN item.stock stock "
			+ "	LEFT JOIN item.user user "
			+ " LEFT JOIN stock.shopsEntity shop "
			+ " LEFT JOIN shop.addressesEntity addr"
			+ " WHERE user.id = :user_id")
	List<CartItemShippingData> findCartItemsShippingDataByUser_Id(@Param("user_id") Long userId);
}
