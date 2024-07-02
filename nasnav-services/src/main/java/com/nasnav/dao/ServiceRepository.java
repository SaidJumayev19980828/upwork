package com.nasnav.dao;

import com.nasnav.persistence.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    Optional<ServiceEntity> findByCode(String code);

    Optional<List<ServiceEntity>> findAllByCodeIn(List<String> codes);

    Set<ServiceEntity> findAllByPackageEntity_Id(Long packageId);

    List<ServiceEntity> findByIdIn(Collection<Long> id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM package_service WHERE service_id = :serviceId", nativeQuery = true)
    void deletePackageServiceByServiceId(@Param("serviceId") Long serviceId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM service_permissions WHERE service_id = :serviceId", nativeQuery = true)
    void deleteServicePermissionsByServiceId(@Param("serviceId") Long serviceId);

}
