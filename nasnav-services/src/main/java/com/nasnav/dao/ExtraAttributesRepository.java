package com.nasnav.dao;

import com.nasnav.persistence.ExtraAttributesEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ExtraAttributesRepository extends CrudRepository<ExtraAttributesEntity, Integer> {

    List<ExtraAttributesEntity> findAll();
    List<ExtraAttributesEntity> findByOrganizationId(Long organizationId);
    Optional<ExtraAttributesEntity> findByNameAndOrganizationId(String name, Long organizationId);

    boolean existsByIdAndOrganizationId(Integer attrId, Long orgId);

    @Transactional
    @Modifying
    void deleteByIdAndOrganizationId(Integer attrId, Long orgId);

    Optional<ExtraAttributesEntity> findByIdAndOrganizationId(Integer id, Long orgId);
}
