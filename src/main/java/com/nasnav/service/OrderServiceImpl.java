package com.nasnav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.response.OrderResponse;
import com.nasnav.response.exception.OrderValidationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import javax.persistence.criteria.Order;

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

    public OrderResponse updateOrder(String orderString){
    	OrderJsonDto orderJson = mapOrderStringToOrderJsonDto(orderString);
    	// TODO: add to basket table
    	Object[] basket = orderJson.getBasket();
    	// check if status is null or new while basket is empty
    	if((orderJson.getStatus() == null || orderJson.getStatus().equals(OrderStatus.NEW.toString())) && (orderJson.getBasket() == null || orderJson.getBasket().length == 0)) {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}
    	
    	// map order object from API to OrdersEntity
    	OrderResponse orderMappingResponse = mapOrderJsonToOrderEntity(orderJson);
    	if(orderMappingResponse.getCode().equals(HttpStatus.NOT_ACCEPTABLE)) {
    		return orderMappingResponse;
    	}
    	    	
        return updateOrCreateOrderEntity(orderMappingResponse.getEntity());
    }

	private OrderResponse mapOrderJsonToOrderEntity(OrderJsonDto orderJson) {
		OrdersEntity entity = new OrdersEntity();
		entity.setId(orderJson.getId());
		if(orderJson.getStatus() != null && !orderJson.getStatus().isEmpty()) {
			OrderStatus statusEnum = OrderStatus.findEnum(orderJson.getStatus());
			if(statusEnum == null) {
				return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.NOT_ACCEPTABLE);
			}
			entity.setStatus(statusEnum.ordinal());
		} else {
			entity.setStatus(0);
		}
		entity.setAddress(orderJson.getAddress());
		entity.setBasket(orderJson.getBasket().toString());
		return new OrderResponse(entity);
	}

	private OrderJsonDto mapOrderStringToOrderJsonDto(String orderString) {
		OrderJsonDto orderJson;
		try {
			orderJson = new ObjectMapper().readValue(orderString, OrderJsonDto.class);
		} catch (IOException e) {
			throw new OrderValidationException("Error Occured while parsing order object", OrderFailedStatus.INVALID_ORDER);
		}
		return orderJson;
	}

	private OrderResponse updateOrCreateOrderEntity(OrdersEntity orderEntity) {
		OrdersEntity createdOrderEntity = orderEntity;
    	if(orderEntity.getId() != null && orderEntity.getId() != 0) {
    		Optional<OrdersEntity> foundOrdersEntity = ordersRepository.findById(orderEntity.getId());
    		if(foundOrdersEntity == null || !foundOrdersEntity.isPresent()) {
    			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
    		}
    		createdOrderEntity = foundOrdersEntity.get();
    	} else {
    		if(orderEntity.getStatus() != null && orderEntity.getStatus() != 0) {
    			return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.NOT_ACCEPTABLE);
    		}
    		createdOrderEntity = new OrdersEntity();
    	
	    	createdOrderEntity.setCreatedAt(new Date(System.currentTimeMillis()));
	    	createdOrderEntity.setUpdatedAt(new Date(System.currentTimeMillis()));
	    	createdOrderEntity.setAmount(new BigDecimal(new Random().nextDouble()));
    	}
    	createdOrderEntity.setStatus(orderEntity.getStatus());
    	if(orderEntity.getBasket() != null && !orderEntity.getBasket().isEmpty()) {
    		createdOrderEntity.setBasket(orderEntity.getBasket().toString());
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
