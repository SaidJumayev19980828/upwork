package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.PackageRegisteredEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, Long> {

    @Query("select p from PackageEntity p " +
            "join PackageRegisteredEntity pr on  p.id = pr.packageEntity.id " +
            "where pr.organization = :organizationEntity")
    Optional<PackageEntity> findPackageByPackageRegisteredOrganization(OrganizationEntity organizationEntity);
}