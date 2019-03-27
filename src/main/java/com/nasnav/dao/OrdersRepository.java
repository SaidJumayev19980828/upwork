package com.nasnav.dao;

import com.nasnav.persistence.OrdersEntity;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface OrdersRepository extends CrudRepository<OrdersEntity, Long> {
	
    Optional<OrdersEntity> findById(long id);
}
