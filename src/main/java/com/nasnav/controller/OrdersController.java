package com.nasnav.controller;

import com.nasnav.service.EmployeeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.OrderRepresentationObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.OrderService;
import com.nasnav.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
@Api(description = "Basket and order management.")
public class OrdersController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
	private UserService userService;

	@Autowired
	private EmployeeUserService employeeUserService;

    @ApiOperation(value = "Create or update an order", nickname = "orderUpdate", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Order created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "update",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> updateOrder(
            @RequestHeader(name = "User-ID") Long userId,
            @RequestHeader(name = "User-Token") String userToken,
            @RequestBody OrderJsonDto orderJson)
            		throws BusinessException {
    	OrderResponse response = this.orderService.updateOrder(orderJson,userId);
        return new ResponseEntity<>(response, response.getCode());
    }

	@ApiOperation(value = "Get information about order", nickname = "orderInfo", code = 201)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
	})
    @GetMapping(value = "/info")
    public ResponseEntity<?> getOrderInfo(
    		@RequestHeader(name = "User-ID", required = true) Long userId,
            @RequestHeader(name = "User-Token", required = true) String userToken,
            @RequestParam(name = "order_id") Long orderId){
		
    	OrderResponse response = this.orderService.getOrderInfo(orderId);
    	
    	if(response.getCode().equals(HttpStatus.OK)) {
        	return new ResponseEntity<>(response.getEntity(), response.getCode());
    	}
    	
    	return new ResponseEntity<>(response, response.getCode());
    }

	@ApiOperation(value = "Get list of orders", nickname = "orderList", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "returned orders list"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "UnAuthorized"),
			//@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> updateOrder(@RequestHeader(name = "User-ID", required = true) Long loggedUserId,
										 @RequestHeader(name = "User-Token", required = true) String userToken,
										 @RequestParam(name = "user_id", required = false) Long userId,  //search parameter
										 @RequestParam(name = "store_id", required = false) Long storeId,
										 @RequestParam(name = "org_id", required = false) Long orgId,
										 @RequestParam(name = "status", required = false) String status) throws BusinessException {
		List<OrderRepresentationObject> response;
		if(userToken == null ||
				(!userService.checkAuthToken(loggedUserId, userToken) && !employeeUserService.checkAuthToken(loggedUserId, userToken))) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		response = this.orderService.getOrdersList(loggedUserId, userToken, userId, storeId, orgId, status);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
