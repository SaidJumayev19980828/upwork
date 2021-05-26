package com.nasnav.controller;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.MetaOrderBasicInfo;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.ReturnRequestSearchParams;
import com.nasnav.dto.request.OrderRejectDTO;
import com.nasnav.dto.request.ReturnRequestRejectDTO;
import com.nasnav.dto.request.order.returned.ReceivedItemsDTO;
import com.nasnav.dto.request.order.returned.ReturnRequestItemsDTO;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.service.OrderReturnService;
import com.nasnav.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Tag(name = "Basket and order management.")
public class OrdersController {

    @Autowired
    private OrderService orderService;


    @Autowired
	private OrderReturnService returnService;
	
    @Operation(description =  "Update an order status", summary = "orderStatusUpdate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Order updated"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "status/update",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestBody OrderJsonDto orderJson) {
    	orderService.updateExistingOrder(orderJson);
    }
    
    
    
    
    

	@Operation(description =  "Get information about order", summary = "orderInfo")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
						   @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public DetailedOrderRepObject getOrderInfo(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam(name = "order_id") Long orderId,
			@RequestParam(name = "details_level", required = false) Integer detailsLevel) throws BusinessException {
		
    	return this.orderService.getOrderInfo(orderId, detailsLevel);
    }


	@Operation(description =  "Get information about order", summary = "orderInfo")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "/meta_order/info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
	public Order getMetaOrderInfo(@RequestHeader(name = "User-Token", required = false) String userToken,
								  @RequestParam(name = "id") Long orderId)  {

		return this.orderService.getMetaOrder(orderId);
	}
	

	@Operation(description =  "Get list of orders", summary = "orderList")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "returned orders list"),
			@ApiResponse(responseCode = " 401" ,description = "UnAuthorized"),
			//@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<DetailedOrderRepObject> getOrdersList(
											@RequestHeader(name = "User-Token", required = false) String userToken,
											OrderSearchParam params) throws BusinessException {
		
		return  this.orderService.getOrdersList(params);
	}



	@Operation(description =  "Get list of user's orders", summary = "metaOrderList")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "/meta_order/list/user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
	public List<MetaOrderBasicInfo> getMetaOrderList(@RequestHeader(name = "User-Token", required = false) String userToken) {
		return orderService.getMetaOrderList();
	}

	

	
	
	@Operation(description =  "Confirm an order", summary = "orderConfirm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Order Confirmed"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "confirm",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrderConfirmResponseDTO confirmOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam("order_id") Long orderId)
            		throws BusinessException {
    	
    	return orderService.confrimOrder(orderId);
    }
	
	
	
	
	@Operation(description =  "Reject an order", summary = "orderReject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Order Confirmed"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "reject",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void rejectOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken
            ,@RequestBody OrderRejectDTO dto)
            		throws BusinessException {
    	orderService.rejectOrder(dto);
    }
	
	
	
	
	
	
	@Operation(description =  "Cancel an order", summary = "orderConfirm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Order Cancelled"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "cancel",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void cancelOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken
            ,@RequestParam("meta_order_id") Long metaOrderId)
            		throws BusinessException {
    	orderService.cancelOrder(metaOrderId);
    }


	@Operation(description =  "Get order return requests", summary = "getOrderReturnRequests")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "return/requests", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
	public ReturnRequestsResponse getOrderReturnRequests(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			ReturnRequestSearchParams params) {

		return returnService.getOrderReturnRequests(params);
	}


	@Operation(description =  "Get order return request", summary = "getOrderReturnRequests")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "return/request", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
	public ReturnRequestDTO getOrderReturnRequest(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam Long id) {

		return returnService.getOrderReturnRequest(id);
	}

	@Operation(description =  "reject returned order items ", summary = "rejectOrderItems")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Order return rejected"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@PostMapping(value = "return/reject")
	public void rejectReturnItems(@RequestHeader(name = "User-Token", required = false) String userToken,
								  @RequestBody ReturnRequestRejectDTO dto) {
		returnService.rejectReturnRequest(dto);
	}


	@Operation(description =  "receive returned order items ", summary = "receiveOrderItems")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Order Cancelled"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@PostMapping(value = "return/received_item")
	public void receiveItems(@RequestHeader(name = "User-Token", required = false) String userToken,
							 @RequestBody ReceivedItemsDTO itemsList) throws BusinessException {
		returnService.receiveItems(itemsList);
	}
	
	
	
	
	
	@Operation(description =  "create return request ", summary = "createReturnRequest")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Return request created"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@PostMapping(value = "return")
	public Long createReturnRequest(@RequestHeader(name = "User-Token", required = false) String userToken,
							 @RequestBody ReturnRequestItemsDTO itemsList) throws BusinessException {
		return returnService.createReturnRequest(itemsList);
	}



	@Operation(description =  "confirm returned order request", summary = "confirmReturnRequest")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Order return confirmed"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@PostMapping(value = "return/confirm")
	public void confirmReturnRequest(@RequestHeader(name = "User-Token", required = false) String userToken,
								  @RequestParam Long id) {
		returnService.confirmReturnRequest(id);
	}
}
