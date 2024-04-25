package com.nasnav.payments.cash;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;


public interface PaymentService {

    PaymentEntity createPaymentForOrder(Long orderId, OrderService.OrderValue orderValue, Long userId) throws BusinessException;

    void finalize(PaymentEntity payment, boolean isYeshtery);
}
