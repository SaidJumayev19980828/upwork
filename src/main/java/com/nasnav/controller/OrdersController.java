package com.nasnav.controller;

import com.nasnav.dto.OrderJsonDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.OrderService;
import com.nasnav.service.UserService;

@RestController
@RequestMapping("/order")
@Api(description = "Basket and order management.")
public class OrdersController {

    @Autowired
    private OrderService orderService;
    
    @Autowired UserService userService;

    @ApiOperation(value = "Register a new user", nickname = "userRegister", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Order created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "update",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> updateOrder(
            @RequestHeader(name = "User-ID") String userId,
            @RequestHeader(name = "User-Token") String userToken,
            @RequestBody OrderJsonDto orderJson)
            		throws BusinessException {
    	OrderResponse response;
    	long parsedUserId = Long.parseLong(userId);
    	if(userToken == null || userService.findUserById(parsedUserId) == null || !userService.checkAuthToken(parsedUserId, userToken)) {
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
    	} else {
        	response = this.orderService.getOrderInfo(orderId);
        	if(response.getCode().equals(HttpStatus.OK)) {
            	return new ResponseEntity<>(response.getEntity(), response.getCode());
        	}
    	} 
    	
    	return new ResponseEntity<>(response, response.getCode());
    }
}
