package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.mastercard.MastercardAccount;
import com.nasnav.payments.mastercard.MastercardService;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Gateway;
import com.nasnav.payments.misc.HTMLConfigurer;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/mcard")
public class PaymentControllerMastercard {

    private static final Logger mastercardLogger = LogManager.getLogger("Payment:MCARD");

    private final MastercardService mastercardService;

    @Autowired
    private AppConfig config;

    @Autowired
    private Commons paymentCommons;

    @Autowired
    public PaymentControllerMastercard(MastercardService service) {
        this.mastercardService = service;
    }

    private String testRefundOrderId = null;
    private boolean testRefundPartial = false;

    @ApiIgnore
    @GetMapping(value = "test/lightbox",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testLightbox(@RequestParam(name = "order_id") Long metaOrderId,
                                          @RequestParam(name = "refund", required = false, defaultValue = "false") boolean refund,
                                          @RequestParam(name = "partial", required = false, defaultValue = "false") boolean partial) throws BusinessException {
        if (!config.develEnvironment) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"message\": \"Not available on production environment\"", HttpStatus.NOT_FOUND);
        }
        String initResult = initPayment(metaOrderId).getBody().toString();
        if (refund) {
            JSONObject jsonResult = new JSONObject(initResult);
            this.testRefundOrderId = jsonResult.getString("order_id");
            this.testRefundPartial = partial;
        }
        String paymentResponseJson = HTMLConfigurer.getConfiguredHtml(initResult, "static/mastercard-lightbox.html");
        return new ResponseEntity<>(paymentResponseJson, HttpStatus.OK);
    }

    @ApiIgnore
    @GetMapping(value = "test/refund")
    public ResponseEntity<?> testRefund() throws BusinessException {
        if (!config.develEnvironment) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"message\": \"Not available on production environment\"", HttpStatus.NOT_FOUND);
        }
        if (this.testRefundOrderId == null) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"message\": \"No test payment set up\"", HttpStatus.NOT_ACCEPTABLE);
        } else {
            PaymentEntity payment = paymentCommons.getPaymentForOrderUid(testRefundOrderId);
            if (payment == null) {
                return new ResponseEntity<>("{\"status\": \"FAILED\", \"message\": \"Payment UID: " + this.testRefundOrderId + " not recognized\"", HttpStatus.NOT_ACCEPTABLE);
            }
            // null - means refund the whole transaction amount
            OrderService.OrderValue ov = null;
            if (testRefundPartial) {
                ov = paymentCommons.getMetaOrderValue(payment.getMetaOrderId());
                ov.amount = ov.amount.divideToIntegralValue(BigDecimal.valueOf(2));
            }
            if (mastercardService.refundTransaction(payment, ov)) {
                return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("{\"status\": \"FAILED\", \"message\": \"Refund failed\"", HttpStatus.NOT_ACCEPTABLE);
    }

    @ApiOperation(value = "Execute the payment after setup and user's data collection", nickname = "mastercardExecute")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Payment completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Some data missing, unable to execute"),
            @io.swagger.annotations.ApiResponse(code = 402, message = "Payment attempted but failed (refused by the gateway)"),
            @io.swagger.annotations.ApiResponse(code = 502, message = "Unable to communicate with the payment gateway"),
    })
    @PostMapping(value = "/execute")
    public ResponseEntity<?> executePayment(@RequestParam(name = "session_id") String sessionId) {
        try {
            mastercardService.execute(sessionId);
            return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
        } catch (BusinessException ex) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"code\": \""
                    + ex.getErrorCode() + "\", \"message\": \"" + ex.getErrorMessage() + "\"}", ex.getHttpStatus());
        }
    }

    @ApiOperation(value = "Verify that the payment initiated via lightbox has successfully completed", nickname = "mastercardVerify")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Payment session set up"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid input data, for example order_uid"),
            @io.swagger.annotations.ApiResponse(code = 408, message = "Confirmation too late, waiting time too long"),
            @io.swagger.annotations.ApiResponse(code = 409, message = "Invalid confirmation code (paymentIndicator)"),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal error, incoherent data in the payment table"),
    })
    @GetMapping(value = "/verify")
    public ResponseEntity<?> checkStatus(
            @RequestParam(name = "order_uid") String orderId,
            @RequestParam(name = "resultIndicator") String resultIndicator
    ) {
        try {
            mastercardService.verifyAndStore(orderId, resultIndicator);
            return  new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
        } catch (BusinessException ex) {
            return new ResponseEntity<>("{\"status\": \"FAILED\", \"code\": \""
                    + ex.getErrorCode() + "\", \"message\": \"" + ex.getErrorMessage() + "\"}", ex.getHttpStatus());
        }
    }

    @ApiOperation(value = "Execute the payment after setup and user data collection", nickname = "mastercardInit")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Payment session set up"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid input data"),
            @io.swagger.annotations.ApiResponse(code = 402, message = "Payment attempted but failed (refused by the gateway)"),
            @io.swagger.annotations.ApiResponse(code = 502, message = "Unable to communicate with the payment gateway"),
    })

    @RequestMapping(value = "initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        OrderSessionResponse response = new OrderSessionResponse();
        response.setSuccess(false);

        MastercardAccount merchantAccount = mastercardService.getAccountForOrder(metaOrderId);

        PaymentEntity payment = mastercardService.initialize(merchantAccount, metaOrderId);
        if (payment != null) {
            try {
                response.setOrderRef(payment.getUid());
                response.setSessionId(payment.getSessionId());
                response.setMerchantId(merchantAccount.getMerchantId());
                response.setApiUrl(merchantAccount.getApiUrl());
                response.setScriptUrl(merchantAccount.getScriptUrl());
                response.setSuccessUrl("/payment/mcard/verify");

                response.setOrderCurrency(payment.getCurrency());
                response.setOrderAmount(payment.getAmount());

                // TODO :  look into
                response.setExecuteUrl("/payment/mcard/execute");
                response.setSuccess(true);
            } catch(Exception ex){
                ex.printStackTrace();
                throw new BusinessException("Unable to set up payment session","",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            ObjectMapper oMap = new ObjectMapper();
            String result = "{}";
            try {
                result = oMap.writeValueAsString(response);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new BusinessException("Unable to initialize Mastercard payment session","",HttpStatus.BAD_GATEWAY);
        }
    }

 }
