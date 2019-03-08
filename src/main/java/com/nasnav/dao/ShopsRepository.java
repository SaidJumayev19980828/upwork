package com.nasnav.dao;

import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShopsRepository extends CrudRepository<ShopsEntity,Long> {
}
