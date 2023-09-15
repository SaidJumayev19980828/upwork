package com.nasnav.dao;

import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
}
