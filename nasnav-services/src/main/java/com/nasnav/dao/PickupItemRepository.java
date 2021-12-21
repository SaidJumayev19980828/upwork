package com.nasnav.dao;

import com.nasnav.persistence.PickupItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Repository
public interface PickupItemRepository extends JpaRepository<PickupItemEntity, Long> {
    @Query("SELECT distinct item "
            + " FROM PickupItemEntity item "
            + "	LEFT JOIN FETCH item.user user"
            + " LEFT JOIN FETCH item.stock stock "
            + " LEFT JOIN FETCH stock.unit unit "
            + " LEFT JOIN FETCH stock.productVariantsEntity variant "
            + " LEFT JOIN FETCH variant.featureValues featureValues"
            + " LEFT JOIN FETCH featureValues.feature feature "
            + " LEFT JOIN FETCH variant.productEntity product "
            + " LEFT JOIN FETCH product.brand brand "
            + " WHERE user.id = :user_id and product.removed = 0 and variant.removed = 0")
    Set<PickupItemEntity> findCurrentPickupItemsByUser_Id(@Param("user_id") Long userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update cart_items set is_wishlist = 0 where id in :ids and is_wishlist = 2 and user_id = :userId")
    void movePickupItemsToCartItems(@Param("ids") Set<Long> pickupItemIds,
                                    @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update cart_items set is_wishlist = 0 where is_wishlist = 2 and user_id = :userId")
    void moveAllPickupItemsToCartItems(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update cart_items set is_wishlist = 2 where id in :ids and is_wishlist = 0 and user_id = :userId")
    void moveCartItemsToPickupItems(@Param("ids") Set<Long> cartItemIds,
                                    @Param("userId") Long userId);
}
