package com.nasnav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.response.exception.OrderValidationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {


    private OrdersRepository ordersRepository;
    
    @Autowired
    public OrderServiceImpl(OrdersRepository ordersRepository){
        this.ordersRepository = ordersRepository;
    }

    public OrderResponse updateOrder(String orderJson){
    	OrdersEntity orderEntity = mapOrderStringToJson(orderJson);
    	System.out.println("status" + orderEntity.getStatus());
		if(orderEntity.getStatus() == 0 && (orderEntity.getBasket() == null || orderEntity.getBasket().isEmpty())) {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}
		    	
        return updateOrCreateOrderEntity(orderEntity);
    }

	private OrdersEntity mapOrderStringToJson(String orderJson) {
		OrdersEntity orderEntity;
		try {
			orderEntity = new ObjectMapper().readValue(orderJson, OrdersEntity.class);
		} catch (IOException e) {
			throw new OrderValidationException("Error Occured while parsing order object", OrderFailedStatus.INVALID_ORDER);
		}
		return orderEntity;
	}

	private OrderResponse updateOrCreateOrderEntity(OrdersEntity orderEntity) {
		OrdersEntity createdOrderEntity = orderEntity;
    	if(orderEntity.getId() != null && orderEntity.getId() != 0) {
    		Optional<OrdersEntity> foundOrdersEntity = ordersRepository.findById(orderEntity.getId());
    		if(foundOrdersEntity == null || !foundOrdersEntity.isPresent()) {
    			System.out.println("entity id is: " + orderEntity.getId().TYPE);
    			System.out.println("entity not present");
    			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
    		}
    		createdOrderEntity = foundOrdersEntity.get();
    	} else {
    		createdOrderEntity = new OrdersEntity();
    	
	    	createdOrderEntity.setCreatedAt(new Date(System.currentTimeMillis()));
	    	createdOrderEntity.setUpdatedAt(new Date(System.currentTimeMillis()));
	    	createdOrderEntity.setAmount(new BigDecimal(new Random().nextDouble()));
    	}
    	createdOrderEntity.setStatus(orderEntity.getStatus());
    	if(orderEntity.getBasket() != null && !orderEntity.getBasket().isEmpty()) {
    		createdOrderEntity.setBasket(orderEntity.getBasket());
    	}
    	createdOrderEntity.setAddress(orderEntity.getAddress());
    	ordersRepository.save(createdOrderEntity);
		return new OrderResponse(createdOrderEntity.getId(), createdOrderEntity.getAmount());
	}

	@Override
	public OrderResponse getOrderInfo(Long orderId) {
		Optional<OrdersEntity> entity = ordersRepository.findById(orderId);
		if(entity.isPresent()) {
			return new OrderResponse(entity.get());
		}
		return new OrderResponse(OrderFailedStatus.UNAUTHENTICATED, HttpStatus.NOT_ACCEPTABLE);
	}

}
