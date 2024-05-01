package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.dto.request.OrderRejectDTO;
import com.nasnav.dto.request.ReturnRequestRejectDTO;
import com.nasnav.dto.request.order.returned.ReceivedItemsDTO;
import com.nasnav.dto.request.order.returned.ReturnRequestItemsDTO;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrdersListResponse;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.service.OrderReturnService;
import com.nasnav.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrderService orderService;
    @Autowired
	private OrderReturnService returnService;

    @PostMapping(value = "status/update", consumes = APPLICATION_JSON_VALUE)
    public void updateOrder(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody OrderJsonDto orderJson) {
    	orderService.updateExistingOrder(orderJson);
    }

    @GetMapping(value = "/info", produces = APPLICATION_JSON_VALUE)
    public DetailedOrderRepObject getOrderInfo(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
											   @RequestParam(name = "order_id") Long orderId,
											   @RequestParam(name = "details_level", required = false) Integer detailsLevel) {
    	return orderService.getOrderInfo(orderId, detailsLevel);
    }

	@GetMapping(value = "/infos/{user_email}", produces = APPLICATION_JSON_VALUE)
	public OrdersListResponse getOrderInfo(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
										   @PathVariable(name = "user_email") String userEmail,
										   @RequestParam(name = "details_level", required = false) Integer detailsLevel) {
		return orderService.getOrdersInfoByUserEmailWithinWeek(userEmail, detailsLevel);
	}

	@GetMapping(value = "/meta_order/info", produces = APPLICATION_JSON_VALUE)
	public Order getMetaOrderInfo(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestParam(name = "id") Long orderId)  {
		return orderService.getMetaOrder(orderId, false);
	}

	@GetMapping(value = "list", produces = APPLICATION_JSON_VALUE)
	public OrdersListResponse getOrdersList(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                            OrderSearchParam params) throws BusinessException {
		return orderService.getOrdersListPageable(params);

	}

	@GetMapping(value = "/filters", produces = APPLICATION_JSON_VALUE)
	public OrdersFiltersResponse getOrdersFilters(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			OrderSearchParam orderSearchParam) throws BusinessException {
		return orderService.getOrdersAvailableFilters(orderSearchParam);
	}

	@GetMapping(value = "track_info", produces = TEXT_PLAIN_VALUE)
	public String trackOrder(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestParam("order_id") Long orderId) {
		return orderService.trackOrder(orderId);
	}

	@GetMapping(value = "/meta_order/list/user", produces = APPLICATION_JSON_VALUE )
	public List<MetaOrderBasicInfo> getMetaOrderList(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
		return orderService.getMetaOrderList();
	}

    @PostMapping(value = "confirm", produces = APPLICATION_JSON_VALUE)
    public OrderConfirmResponseDTO confirmOrder(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestParam("order_id") Long orderId) {
    	return orderService.confirmOrder(orderId, null);
    }

    @PostMapping(value = "reject", produces = APPLICATION_JSON_VALUE)
    public void rejectOrder(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody OrderRejectDTO dto) {
    	orderService.rejectOrder(dto);
    }

    @PostMapping(value = "cancel", produces = APPLICATION_JSON_VALUE)
    public void cancelOrder(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestParam("meta_order_id") Long metaOrderId) {
    	orderService.cancelOrder(metaOrderId, false);
    }

	@GetMapping(value = "return/requests", produces = APPLICATION_JSON_VALUE )
	public ReturnRequestsResponse getOrderReturnRequests(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, ReturnRequestSearchParams params) {
		return returnService.getOrderReturnRequests(params);
	}

	@GetMapping(value = "return/request", produces = APPLICATION_JSON_VALUE )
	public ReturnRequestDTO getOrderReturnRequest(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestParam Long id) {
		return returnService.getOrderReturnRequest(id);
	}

	@PostMapping(value = "return/reject")
	public void rejectReturnItems(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody ReturnRequestRejectDTO dto) {
		returnService.rejectReturnRequest(dto);
	}

	@PostMapping(value = "return/received_item")
	public void receiveItems(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody ReceivedItemsDTO itemsList) {
		returnService.receiveItems(itemsList);
	}

	@PostMapping(value = "return")
	public Long createReturnRequest(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody ReturnRequestItemsDTO itemsList) {
		return returnService.createReturnRequest(itemsList);
	}

	@PostMapping(value = "return/confirm")
	public void confirmReturnRequest(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestParam Long id) {
		returnService.confirmReturnRequest(id);
	}
}
