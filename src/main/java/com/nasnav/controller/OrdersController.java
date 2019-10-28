package com.nasnav.controller;

import java.util.List;

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

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.OrderService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/order")
@Api(description = "Basket and order management.")
public class OrdersController {

    @Autowired
    private OrderService orderService;
    
    
	
    @ApiOperation(value = "Create or update an order", nickname = "orderUpdate", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Order created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "update",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrderResponse updateOrder(
            
            @RequestHeader(name = "User-Token") String userToken,
            @RequestBody OrderJsonDto orderJson)
            		throws BusinessException {
    	
    	return orderService.handleOrder(orderJson);        
    }
    
    
    
    
    

	@ApiOperation(value = "Get information about order", nickname = "orderInfo", code = 201)
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
						   @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<?> getOrderInfo(
            @RequestHeader(name = "User-Token") String userToken,
            @RequestParam(name = "order_id") Long orderId) {
    	OrderResponse response = this.orderService.getOrderInfo(orderId);
    	if(response.getCode().equals(HttpStatus.OK)) {
        	return new ResponseEntity<>(response.getDetailedOrder(), response.getCode());
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
	public List<DetailedOrderRepObject> getOrdersList(
											@RequestHeader(name = "User-Token") String userToken,
											@RequestParam(name = "user_id", required = false) Long userId,  //search parameter
											@RequestParam(name = "store_id", required = false) Long storeId,
											@RequestParam(name = "org_id", required = false) Long orgId,
											@RequestParam(name = "status", required = false) String status) throws BusinessException {
		
		return  this.orderService.getOrdersList(userToken, userId, storeId, orgId, status);
	}
}
