package com.nasnav.dao;

import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.AdvertisementProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AdvertisementRepository extends JpaRepository<AdvertisementEntity, Long>, JpaSpecificationExecutor<AdvertisementEntity> {
    @Query("select ape from AdvertisementProductEntity ape where ape.advertisement.id= :advertisementId and ape.product.id in :productsInPost")
    List<AdvertisementProductEntity> findAdvertisementProducts(@Param("advertisementId") Long advertisementId, @Param("productsInPost") Set<Long> productsInPost);
}
