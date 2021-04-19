package com.nasnav.controller;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.HTMLConfigurer;
import com.nasnav.payments.rave.RaveAccount;
import com.nasnav.payments.rave.RaveService;
import com.nasnav.persistence.PaymentEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/rave")
public class PaymentControllerRave {

    private static final Logger reveLogger = LogManager.getLogger("Payment:RAVE");

    private final RaveService raveService;

    @Autowired
    public PaymentControllerRave(RaveService raveService) {
        this.raveService = raveService;
    }

    @Operation(hidden = true)
    @GetMapping(value = "/test/payment",produces= MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        String initResult = initPayment(metaOrderId).getBody().toString();
        return new ResponseEntity<>(HTMLConfigurer.getConfiguredHtml(initResult, "static/rave.html"), HttpStatus.OK);
    }

    @GetMapping(value = "/success")
    public ResponseEntity<?> paymentSuccess(
            @RequestParam(name = "ref") String flwRef,
            @RequestParam(name = "uid") String uid) {
        try {
            raveService.verifyAndStore(flwRef, uid);
            return  new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
        } catch (BusinessException ex) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"code\": \""
                    + ex.getErrorCode() + "\", \"message\": \"" + ex.getErrorMessage() + "\"}", ex.getHttpStatus());
        }
    }


    @RequestMapping(value = "/failure")
    public ResponseEntity<?> paymentFailed() {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"code\": \""
                    + "PAYMENT_FAILED" + "\", \"message\": \"General failure\"}", HttpStatus.BAD_GATEWAY);
    }

    @RequestMapping(value = "/initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        if (metaOrderId == null || metaOrderId < 0) {
            throw new BusinessException("Invalid order ID", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
        }

        RaveAccount merchantAccount = raveService.getAccountForOrder(metaOrderId);
        PaymentEntity payment = raveService.initialize(merchantAccount, metaOrderId);

        JSONObject data = new JSONObject();
        data.put("order_amount", payment.getAmount());
        data.put("order_currency", payment.getCurrency());
        data.put("order_id", payment.getUid());
        data.put("public_key", merchantAccount.getPublicKey());
        data.put("script_url", merchantAccount.getScriptUrl());
        data.put("success_url", merchantAccount.getSuccessUrl());
        data.put("failure_url", merchantAccount.getFailureUrl());

        return new ResponseEntity<>(data.toString(), HttpStatus.OK);
    }

 }
