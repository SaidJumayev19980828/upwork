package com.nasnav.dao;

import com.nasnav.persistence.AdvertisementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<AdvertisementEntity, Long> {
}
