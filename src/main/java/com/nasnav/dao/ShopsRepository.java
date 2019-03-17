package com.nasnav.dao;

import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShopsRepository extends CrudRepository<ShopsEntity,Long> {

    List<ShopsEntity> findByOrganizationEntity_Id(Long organizationId);
}
