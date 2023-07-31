package com.nasnav.dao;

import com.nasnav.persistence.AdvertisementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvertisementRepository extends JpaRepository<AdvertisementEntity, Long> {
    @Query("select pe.advertisement from PostEntity pe where pe.id= :postId")
    AdvertisementEntity findAdvertisementEntitiesByPostId(@Param("postId") Long postId);
}
