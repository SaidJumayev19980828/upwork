package com.nasnav.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.BasketsEntity;

public interface BasketRepository extends CrudRepository<BasketsEntity, Long>{

    List<BasketsEntity> findByOrdersEntity_Id(Long orderId);

    void deleteByOrdersEntity_Id(Long orderId);
}
