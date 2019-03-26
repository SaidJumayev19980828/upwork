package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.OrderService;
import com.nasnav.service.UserService;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrderService orderService;
    
    @Autowired UserService userService;

    @PostMapping(path= "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateOrder(@RequestHeader(name = "User-ID", required = true) long userId,
            @RequestHeader(name = "User-Token", required = true) String userToken,
            @RequestBody String orderJson) 
            		throws BusinessException {
    	OrderResponse response;
    	if(userToken == null || userService.findUserById(userId) == null) {
    		response = new OrderResponse(OrderFailedStatus.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED);
    	} else {
        	response = this.orderService.updateOrder(orderJson);	
    	}        
        return new ResponseEntity<>(response, response.getCode());
    }
    
    @GetMapping(value = "/info")
    public ResponseEntity<?> getOrderInfo(@RequestHeader(name = "User-ID", required = true) long userId,
            @RequestHeader(name = "User-Token", required = true) String userToken,
            @RequestParam(name = "order_id") Long orderId){
    	OrderResponse response;
    	if(userToken == null || userService.findUserById(userId) == null) {
    		response = new OrderResponse(OrderFailedStatus.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED);
    		return new ResponseEntity<>(response, response.getCode());
    	} else {
        	response = this.orderService.getOrderInfo(orderId);
        	if(response.getCode().equals(HttpStatus.OK)) {
            	return new ResponseEntity<>(response.getEntity(), response.getCode());
        	}
    	} 
    	
    	return new ResponseEntity<>(response, response.getCode());
    }
}
