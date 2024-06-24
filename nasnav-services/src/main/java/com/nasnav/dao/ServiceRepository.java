package com.nasnav.dao;

import com.nasnav.persistence.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
