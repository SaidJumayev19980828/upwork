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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrderService orderService;
    @Autowired
	private OrderReturnService returnService;

    @PostMapping(value = "status/update", consumes = APPLICATION_JSON_VALUE)
    public void updateOrder(@RequestHeader(name = "User-Token", required = false) String userToken,
            				@RequestBody OrderJsonDto orderJson) {
    	orderService.updateExistingOrder(orderJson);
    }

    @GetMapping(value = "/info", produces = APPLICATION_JSON_VALUE)
    public DetailedOrderRepObject getOrderInfo(@RequestHeader(name = "User-Token", required = false) String userToken,
											   @RequestParam(name = "order_id") Long orderId,
											   @RequestParam(name = "details_level", required = false) Integer detailsLevel) throws BusinessException {
    	return this.orderService.getOrderInfo(orderId, detailsLevel);
    }

	@GetMapping(value = "/meta_order/info", produces = APPLICATION_JSON_VALUE)
	public Order getMetaOrderInfo(@RequestHeader(name = "User-Token", required = false) String userToken,
								  @RequestParam(name = "id") Long orderId)  {
		return this.orderService.getMetaOrder(orderId);
	}

	@GetMapping(value = "list", produces = APPLICATION_JSON_VALUE)
	public List<DetailedOrderRepObject> getOrdersList(@RequestHeader(name = "User-Token", required = false) String userToken,
													  OrderSearchParam params) throws BusinessException {
		return  this.orderService.getOrdersList(params);
	}

	@GetMapping(value = "track_info", produces = APPLICATION_JSON_VALUE)
	public String trackOrder(@RequestHeader(name = "User-Token", required = false) String userToken,
									  @RequestParam("order_id") Long orderId) {
		return orderService.trackOrder(orderId);
	}

	@GetMapping(value = "/meta_order/list/user", produces = APPLICATION_JSON_VALUE )
	public List<MetaOrderBasicInfo> getMetaOrderList(@RequestHeader(name = "User-Token", required = false) String userToken) {
		return orderService.getMetaOrderList();
	}

    @PostMapping(value = "confirm", produces = APPLICATION_JSON_VALUE)
    public OrderConfirmResponseDTO confirmOrder(@RequestHeader(name = "User-Token", required = false) String userToken,
            									@RequestParam("order_id") Long orderId) {
    	return orderService.confrimOrder(orderId);
    }

    @PostMapping(value = "reject", produces = APPLICATION_JSON_VALUE)
    public void rejectOrder(@RequestHeader(name = "User-Token", required = false) String userToken,
							@RequestBody OrderRejectDTO dto) {
    	orderService.rejectOrder(dto);
    }

    @PostMapping(value = "cancel", produces = APPLICATION_JSON_VALUE)
    public void cancelOrder(@RequestHeader(name = "User-Token", required = false) String userToken,
							@RequestParam("meta_order_id") Long metaOrderId) {
    	orderService.cancelOrder(metaOrderId);
    }

	@GetMapping(value = "return/requests", produces = APPLICATION_JSON_VALUE )
	public ReturnRequestsResponse getOrderReturnRequests(@RequestHeader(name = "User-Token", required = false) String userToken,
														 ReturnRequestSearchParams params) {
		return returnService.getOrderReturnRequests(params);
	}

	@GetMapping(value = "return/request", produces = APPLICATION_JSON_VALUE )
	public ReturnRequestDTO getOrderReturnRequest(@RequestHeader(name = "User-Token", required = false) String userToken,
												  @RequestParam Long id) {
		return returnService.getOrderReturnRequest(id);
	}

	@PostMapping(value = "return/reject")
	public void rejectReturnItems(@RequestHeader(name = "User-Token", required = false) String userToken,
								  @RequestBody ReturnRequestRejectDTO dto) {
		returnService.rejectReturnRequest(dto);
	}

	@PostMapping(value = "return/received_item")
	public void receiveItems(@RequestHeader(name = "User-Token", required = false) String userToken,
							 @RequestBody ReceivedItemsDTO itemsList) {
		returnService.receiveItems(itemsList);
	}

	@PostMapping(value = "return")
	public Long createReturnRequest(@RequestHeader(name = "User-Token", required = false) String userToken,
							 		@RequestBody ReturnRequestItemsDTO itemsList) {
		return returnService.createReturnRequest(itemsList);
	}

	@PostMapping(value = "return/confirm")
	public void confirmReturnRequest(@RequestHeader(name = "User-Token", required = false) String userToken,
								  	 @RequestParam Long id) {
		returnService.confirmReturnRequest(id);
	}
}
