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
    
    private UserRepository userRepository;

    @Autowired
    public OrderServiceImpl(OrdersRepository ordersRepository){
        this.ordersRepository = ordersRepository;
    }

    public OrderResponse updateOrder(String orderJson){
    	OrdersEntity orderEntity;
		try {
			orderEntity = new ObjectMapper().readValue(orderJson, OrdersEntity.class);
		} catch (IOException e) {
			throw new OrderValidationException("Error Occured while parsing order object", OrderFailedStatus.UNAUTHENTICATED);
		}
		if(orderEntity.getStatus() == 0 && orderEntity.getBasket().isEmpty()) {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}
		    	
        return updateOrCreateOrdeEntity(orderEntity);
    }

	private OrderResponse updateOrCreateOrdeEntity(OrdersEntity orderEntity) {
		OrdersEntity createdOrderEntity = orderEntity;
    	if(orderEntity.getId() != null && orderEntity.getId() != 0) {
    		Optional<OrdersEntity> foundOrdersEntity = ordersRepository.findById(orderEntity.getId());
    		if(foundOrdersEntity == null || !foundOrdersEntity.isPresent()) {
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
    	if(!orderEntity.getBasket().isEmpty()) {
    		createdOrderEntity.setBasket(orderEntity.getBasket());
    	}
    	createdOrderEntity.setAddress(orderEntity.getAddress());
    	ordersRepository.save(createdOrderEntity);
		return new OrderResponse(createdOrderEntity.getId(), createdOrderEntity.getAmount());
	}

	@Override
	public OrderResponse getOrderInfo(Long orderId) {
		Optional<OrdersEntity> entity = ordersRepository.findById(orderId);
		if(!entity.isPresent()) {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		} else {
			return new OrderResponse(entity.get());
		}
	}

}
