package com.nasnav.dao;

import com.nasnav.persistence.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AdvertisementRepository extends JpaRepository<AdvertisementEntity, Long>, JpaSpecificationExecutor<AdvertisementEntity> {
    @Query("select ape from AdvertisementProductEntity ape where ape.advertisement.id= :advertisementId and ape.product.id in :productsInPost")
    List<AdvertisementProductEntity> findAdvertisementProducts(@Param("advertisementId") Long advertisementId, @Param("productsInPost") Set<Long> productsInPost);

    @Query("SELECT entity " +
            "FROM AdvertisementEntity entity " +
            "WHERE (:name IS NULL OR entity.name like %:name%) " +
            "AND  (:orgId IS NULL OR entity.organization.id =:orgId) " +
            "AND  ( to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "        to_timestamp(CAST(entity.fromDate as text), 'yyyy-MM-dd HH24:MI:SS') >= " +
            "       to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            ") " +
            "AND ( to_timestamp(CAST(:toDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "    to_timestamp(CAST(entity.toDate as text), 'yyyy-MM-dd HH24:MI:SS') <= " +
            "    to_timestamp(CAST(:toDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            ")"
    )
    PageImpl<AdvertisementEntity> getAllByDateBetweenAndStatusEqualsAndNameIfNotNull(Long orgId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate, String name, Pageable pageable);
}
