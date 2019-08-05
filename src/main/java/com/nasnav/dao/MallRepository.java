package com.nasnav.dao;

import com.nasnav.persistence.MallsEntity;
import org.springframework.data.repository.CrudRepository;

public interface MallRepository extends CrudRepository<MallsEntity, Long> {

}
