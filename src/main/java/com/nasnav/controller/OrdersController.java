package com.nasnav.controller;

import java.util.List;

import com.nasnav.request.OrderSearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.response.OrderConfrimResponseDTO;
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


	@ApiOperation(value = "Create an order", nickname = "orderCreation", code = 201)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Order created"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@PostMapping(value = "create",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public OrderResponse createOrder(@RequestHeader(name = "User-Token", required = false) String userToken,
									 @RequestBody OrderJsonDto orderJson) throws BusinessException {

		return orderService.createNewOrder(orderJson);
	}
    
	
    @ApiOperation(value = "Update an order", nickname = "orderUpdate")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Order updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "update",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrderResponse updateOrder(
            
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestBody OrderJsonDto orderJson)
            		throws BusinessException {
    	
    	return orderService.updateExistingOrder(orderJson);
    }
    
    
    
    
    

	@ApiOperation(value = "Get information about order", nickname = "orderInfo", code = 201)
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
						   @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public DetailedOrderRepObject getOrderInfo(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam(name = "order_id") Long orderId,
			@RequestParam(name = "details_level", required = false) Integer detailsLevel) throws BusinessException {
		
    	return this.orderService.getOrderInfo(orderId, detailsLevel);
    }
	
	
	
	
	

	@ApiOperation(value = "Get list of orders", nickname = "orderList", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "returned orders list"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "UnAuthorized"),
			//@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<DetailedOrderRepObject> getOrdersList(
											@RequestHeader(name = "User-Token", required = false) String userToken,
											OrderSearchParam params) throws BusinessException {
		
		return  this.orderService.getOrdersList(params);
	}
	
	
	
	
	
	@ApiOperation(value = "Get the current new order", nickname = "currentOrder", code = 201)
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
						   @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public DetailedOrderRepObject getCurrentNewOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "details_level", required = false) Integer detailsLevel) throws BusinessException {
		
    	return this.orderService.getCurrentOrder(detailsLevel);
    }
	
	
	
	
	
	
	@ApiOperation(value = "delete current order", nickname = "deleteCurrentOrder", code = 201)
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
						   @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
    @DeleteMapping(value = "/current", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public void deleteNewOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken) {
		
    	 this.orderService.deleteCurrentOrders();    	
    }


	@ApiOperation(value = "delete list of orders", nickname = "deleteOrders", code = 200)
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
	@DeleteMapping( produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
	public void deleteOrders(@RequestHeader(name = "User-Token", required = false) String userToken,
							 @RequestParam("order_ids") List<Long> orderIds) throws BusinessException {

		this.orderService.deleteOrders(orderIds);
	}
	
	
	
	
	@ApiOperation(value = "Confirm an order", nickname = "orderConfirm")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Order Confirmed"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "confirm",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrderConfrimResponseDTO confrimOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam("order_id") Long orderId)
            		throws BusinessException {
    	
    	return orderService.confrimOrder(orderId);
    }
}
