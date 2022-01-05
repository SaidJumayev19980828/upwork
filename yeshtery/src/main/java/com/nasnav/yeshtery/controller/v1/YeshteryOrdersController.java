package com.nasnav.yeshtery.controller.v1;

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
import com.nasnav.service.*;
import com.nasnav.yeshtery.YeshteryConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.nasnav.yeshtery.controller.v1.YeshteryOrdersController.API_PATH;

@RestController
@RequestMapping(API_PATH)
@Tag(name = "Yeshtery Basket and order management.")
public class YeshteryOrdersController {

	static final String API_PATH = YeshteryConstants.API_PATH +"/order/";

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
    @PostMapping(value = "status/update")
    public void updateOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestBody OrderJsonDto orderJson) {
    	orderService.updateExistingOrder(orderJson);
    }

	@Operation(description =  "Get information about order", summary = "orderInfo")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
						   @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
    @GetMapping(value = "info" )
    public DetailedOrderRepObject getOrderInfo(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam(name = "order_id") Long orderId,
			@RequestParam(name = "details_level", required = false) Integer detailsLevel) throws BusinessException {
		
    	return orderService.getYeshteryOrderInfo(orderId, detailsLevel);
    }

	@Operation(description =  "Get information about order", summary = "orderInfo")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "meta_order/info" )
	public Order getMetaOrderInfo(@RequestHeader(name = "User-Token", required = false) String userToken,
								  @RequestParam(name = "id") Long orderId)  {

		return orderService.getYeshteryMetaOrder(orderId, true);
	}

	@Operation(description =  "Get list of orders", summary = "orderList")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "returned orders list"),
			@ApiResponse(responseCode = " 401" ,description = "UnAuthorized"),
			//@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@GetMapping(value = "list")
	public List<DetailedOrderRepObject> getOrdersList(
											@RequestHeader(name = "User-Token", required = false) String userToken,
											OrderSearchParam params) throws BusinessException {
		return  orderService.getYeshteryOrdersList(params);
	}

	@Operation(description =  "Get list of user's orders", summary = "metaOrderList")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "meta_order/list/user" )
	public List<MetaOrderBasicInfo> getMetaOrderList(@RequestHeader(name = "User-Token", required = false) String userToken) {
		return orderService.getYeshteryMetaOrderList();
	}

	@Operation(description =  "Confirm an order", summary = "orderConfirm")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Order Confirmed"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@PostMapping(value = "confirm")
	public OrderConfirmResponseDTO confirmOrder(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam("order_id") Long orderId,
			@RequestParam(value = "pin_code", required = false) String pinCode,
			@RequestParam(value = "points_amount", required = false) BigDecimal pointsAmount) {

		return orderService.confirmOrder(orderId, pinCode, pointsAmount);
	}


	@ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Order Confirmed"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })

    @PostMapping(value = "reject")
    public void rejectOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken
            ,@RequestBody OrderRejectDTO dto) {
    	orderService.rejectOrder(dto);
    }

	@Operation(description =  "Cancel an order", summary = "orderConfirm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Order Cancelled"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "cancel")
    public void cancelOrder(
            @RequestHeader(name = "User-Token", required = false) String userToken
            ,@RequestParam("meta_order_id") Long metaOrderId) {
    	orderService.cancelYeshteryOrder(metaOrderId);
    }


	@Operation(description =  "Get order return requests", summary = "getOrderReturnRequests")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "return/requests" )
	public ReturnRequestsResponse getOrderReturnRequests(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			ReturnRequestSearchParams params) {
		return returnService.getYeshteryOrderReturnRequests(params);
	}


	@Operation(description =  "Get order return request", summary = "getOrderReturnRequests")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "return/request" )
	public ReturnRequestDTO getOrderReturnRequest(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam Long id) {

		return returnService.getYeshteryOrderReturnRequest(id);
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
		returnService.rejectYeshteryReturnRequest(dto);
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
		return returnService.createYeshteryReturnRequest(itemsList);
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
		returnService.confirmYeshteryReturnRequest(id);
	}
}
