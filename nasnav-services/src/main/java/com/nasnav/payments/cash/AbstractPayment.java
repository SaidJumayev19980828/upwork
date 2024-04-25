package com.nasnav.payments.cash;

import com.nasnav.payments.misc.Commons;
import com.nasnav.persistence.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AbstractPayment {

    @Autowired
    private Commons commons;

    protected void finalizePayment(PaymentEntity payment, boolean yeshteryMetaOrder) {
       commons.finalizePayment(payment, yeshteryMetaOrder);
    }
}
