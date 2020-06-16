package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.dto.query.result.CartItemData;

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
	
}
