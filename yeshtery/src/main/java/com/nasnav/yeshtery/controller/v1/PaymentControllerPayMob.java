package com.nasnav.yeshtery.controller.v1;


import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.paymob.PaymobService;
import com.nasnav.payments.paymob.PaymobSource;
import com.nasnav.payments.paymob.TokenResponse;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.yeshtery.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.O$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(PaymentControllerPayMob.API_PATH)
public class PaymentControllerPayMob {

    static final String API_PATH = YeshteryConstants.API_PATH +"/payment/paymob";




    @Autowired
    private MetaOrderRepository ordersRepository;

    @Autowired
    private PaymobService paymobService;

    @PostMapping("init")
    public ResponseEntity<String> init(@RequestParam(name = "order_id") Long metaOrderId, @RequestBody PaymobSource source) throws BusinessException {

        Optional<MetaOrderEntity> metaOrder = ordersRepository.findByMetaOrderId(metaOrderId);
        if (metaOrder.isEmpty()) {
            throw new RuntimeBusinessException(NOT_FOUND, O$0001, metaOrderId);
        }
        try {
            TokenResponse token = paymobService.init(metaOrder.get(), source);
            return new ResponseEntity<>("{\"status\": \"SUCCESS\", \"token\": \""+token.getToken()+"\"", HttpStatus.OK);

        } catch (BusinessException ex) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"code\": \""
                    + ex.getErrorCode() + "\", \"message\": \"" + ex.getErrorMessage() + "\"}", ex.getHttpStatus());
        }
    }


    @PostMapping("confirm")
    public ResponseEntity<String> confirm(@RequestParam(name = "uid") String uid) {
        try {
            paymobService.verifyAndStore(uid);
            return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
        } catch (BusinessException ex) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"code\": \""
                    + ex.getErrorCode() + "\", \"message\": \"" + ex.getErrorMessage() + "\"}", ex.getHttpStatus());
        }
    }

}
