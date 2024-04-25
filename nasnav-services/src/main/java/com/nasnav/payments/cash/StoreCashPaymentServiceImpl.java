package com.nasnav.payments.cash;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
/**
 * Store Cache payment service when the user pay cash on the store
 * this class is used with the two-step checkout as the user will be in store
 */
@Service("storeCashPaymentService")
public class StoreCashPaymentServiceImpl extends AbstractPayment implements PaymentService {
    private static final Logger storeChashLogger = LogManager.getLogger(StoreCashPaymentServiceImpl.class);


    /**
     * <p>creating the payment entity with type IN_STORE_CASH.
     * which is a type when user pay cash on the store
     * </p>
     */
    @Override
    public PaymentEntity createPaymentForOrder(Long orderId, OrderService.OrderValue orderValue, Long userId) throws BusinessException {
        PaymentEntity payment = new PaymentEntity();
        payment.setOperator("IN_STORE_CASH");
        payment.setUid(Tools.getOrderUid(orderId, storeChashLogger));
        payment.setExecuted(Date.from(LocalDateTime.now()
                 .atZone(ZoneId.systemDefault())
                .toInstant()));
        payment.setStatus(PaymentStatus.PAID);
        payment.setAmount(orderValue.amount);
        payment.setCurrency(orderValue.currency);
        payment.setUserId(userId);
        payment.setMetaOrderId(orderId);
        return payment;
    }

    @Override
    public void finalize(PaymentEntity payment, boolean isYeshtery) {
        finalizePayment(payment, isYeshtery);
    }


}
