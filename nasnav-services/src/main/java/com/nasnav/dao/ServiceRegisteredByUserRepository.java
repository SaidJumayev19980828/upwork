package com.nasnav.dao;


import com.nasnav.persistence.PackageRegisteredEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRegisteredByUserRepository extends JpaRepository<PackageRegisteredEntity, Long> {

    @Query("SELECT p FROM PackageRegisteredEntity p  WHERE p.packageEntity.id = :package_id ")
    List<PackageRegisteredEntity> findByPackageId(Long package_id);

}
