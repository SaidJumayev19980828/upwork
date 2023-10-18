package com.nasnav.dao;

import com.nasnav.persistence.AdvertisementProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementProductRepository extends JpaRepository<AdvertisementProductEntity, Long> {
    void deleteAllByAdvertisement_Id(Long id);
}