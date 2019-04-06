package com.nasnav.dao;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.OrdersEntity;

public interface OrderRepository extends CrudRepository<OrdersEntity, Long>{

}
