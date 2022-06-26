package com.nasnav.yeshtery.controller.v1;


import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.paymob.*;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.yeshtery.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.O$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(PaymentControllerPayMob.API_PATH)
public class PaymentControllerPayMob {
    static final String API_PATH = YeshteryConstants.API_PATH +"/payment/paymob";
    @Autowired
    private MetaOrderRepository ordersRepository;
    @Autowired
    private PaymobService paymobService;

    @PostMapping(value = "card/init", produces = APPLICATION_JSON_VALUE)
    public LinkedHashMap<String, String> cardInitialize(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        Optional<MetaOrderEntity> metaOrder = ordersRepository.findByMetaOrderId(metaOrderId);
        if (metaOrder.isEmpty()) {
            throw new RuntimeBusinessException(NOT_FOUND, O$0001, metaOrderId);
        }
        return paymobService.payMobCardInit(metaOrder.get());
    }

    @PostMapping(value = "callback/card/confirm")
    public void cardConfirmCallback(@RequestParam String hmac,
                                    @RequestBody WebhookCallbackResponse response) throws BusinessException {
        paymobService.confirmPaymentThroughCallback(hmac, response, true);
    }

    @PostMapping("card/confirm")
    public ResponseEntity<String> cardConfirm(@RequestParam(name = "token") String uid) throws BusinessException {
        paymobService.verifyAndStore(uid, true);
        return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
    }

}
