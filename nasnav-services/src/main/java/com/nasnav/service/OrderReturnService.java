package com.nasnav.service;

import com.nasnav.dto.ReturnRequestSearchParams;
import com.nasnav.dto.request.ReturnRequestRejectDTO;
import com.nasnav.dto.request.order.returned.ReceivedItemsDTO;
import com.nasnav.dto.request.order.returned.ReturnRequestItemsDTO;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.response.ReturnRequestsResponse;

public interface OrderReturnService {
    String ORDER_RETURN_CONFIRM_SUBJECT = "Your Order return has been confirmed!";
    String ORDER_RETURN_REJECT_SUBJECT = "Sorry! Your Order return has been rejected!";
    String ORDER_RETURN_NOTIFY_SUBJECT = "New Order return request [%d] has been created!";
    String ORDER_RETURN_RECEIVE_SUBJECT = "Your returned items has been received by the store!";

    ReturnRequestsResponse getOrderReturnRequests(ReturnRequestSearchParams params);
    ReturnRequestDTO getOrderReturnRequest(Long id);
    void confirmReturnRequest(Long id);
    void rejectReturnRequest(ReturnRequestRejectDTO dto);
    Long createReturnRequest(ReturnRequestItemsDTO itemsList);
    void receiveItems(ReceivedItemsDTO returnedItems);
    boolean isReturnable(BasketsEntity basketsEntity);
    ReturnRequestsResponse getYeshteryOrderReturnRequests(ReturnRequestSearchParams params);
    ReturnRequestDTO getYeshteryOrderReturnRequest(Long id);
    void confirmYeshteryReturnRequest(Long id);
    void rejectYeshteryReturnRequest(ReturnRequestRejectDTO dto);
    Long createYeshteryReturnRequest(ReturnRequestItemsDTO itemsList);

}
