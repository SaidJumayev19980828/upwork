package com.nasnav.controller;


import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.paymob.PaymobService;
import com.nasnav.payments.paymob.PaymobSource;
import com.nasnav.payments.paymob.RetrieveTransactionResponse;
import com.nasnav.persistence.MetaOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.O$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/paymob")
public class PaymentControllerPayMob {


    @Autowired
    private MetaOrderRepository ordersRepository;

    @Autowired
    private PaymobService paymobService;

    @PostMapping("card_init")
    public String init(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {

        Optional<MetaOrderEntity> metaOrder = ordersRepository.findByMetaOrderId(metaOrderId);
        if (metaOrder.isEmpty()) {
            throw new RuntimeBusinessException(NOT_FOUND, O$0001, metaOrderId);
        }
        return paymobService.payMobCardInit(metaOrder.get());

    }


    @PostMapping("card_confirm")
    public ResponseEntity<String> confirm(@RequestParam(name = "uid") String uid) throws BusinessException {
            RetrieveTransactionResponse data = paymobService.verifyAndStore(uid, false);
            return new ResponseEntity<>("{\"status\": \"SUCCESS\", " +
                    "\"transaction_no\":\""+data.getId()+"\"" +
                    "}", HttpStatus.OK);

    }
}
