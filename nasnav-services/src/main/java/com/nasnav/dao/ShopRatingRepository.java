package com.nasnav.dao;
import com.nasnav.persistence.ProductRating;
import com.nasnav.persistence.ShopRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRatingRepository  extends JpaRepository<ShopRating, Long> {
     Optional<ShopRating> findByShopIdAndUserId(Long shopId, Long userId);
    
}
