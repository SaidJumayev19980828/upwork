package com.nasnav.dao;


import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageRegisteredEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PackageRegisteredRepository extends JpaRepository<PackageRegisteredEntity, Long> {

    @Query("SELECT p FROM PackageRegisteredEntity p  WHERE p.packageEntity.id = :package_id ")
    List<PackageRegisteredEntity> findByPackageId(Long package_id);

    Optional<PackageRegisteredEntity> findByOrganization(OrganizationEntity organization);
}
