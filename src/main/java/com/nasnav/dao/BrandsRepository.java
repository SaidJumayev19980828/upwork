package com.nasnav.dao;

import com.nasnav.persistence.BrandsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrandsRepository extends CrudRepository<BrandsEntity,Long> {

    List<BrandsEntity> findByOrganizationEntity_Id(Long organizationEntity_Id);
}
