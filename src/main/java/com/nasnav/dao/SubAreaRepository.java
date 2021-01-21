package com.nasnav.dao;

import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.SubAreasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SubAreaRepository extends JpaRepository<SubAreasEntity, Long> {
    List<SubAreasEntity> findByAreaAndOrganization_Id(AreasEntity area, Long organizationId);
}
