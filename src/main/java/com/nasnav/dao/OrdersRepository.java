package com.nasnav.dao;

import com.nasnav.persistence.OrdersEntity;

import org.springframework.data.repository.CrudRepository;


public interface OrdersRepository extends CrudRepository<OrdersEntity, Long> {
	
    OrdersEntity findById(long id);
}
