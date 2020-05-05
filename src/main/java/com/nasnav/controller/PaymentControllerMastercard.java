package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.mastercard.MastercardSession;
import com.nasnav.payments.misc.HTMLConfigurer;
import com.nasnav.payments.misc.Tools;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Properties;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/mcard")
public class PaymentControllerMastercard {

    private static final Logger mastercardLogger = LogManager.getLogger("Payment:MCARD");

    private final OrdersRepository ordersRepository;

    private final MastercardSession session;

    @Autowired
    private AppConfig config;

    @Autowired
    private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;

    @Autowired
    public PaymentControllerMastercard(
            OrdersRepository ordersRepository,
            PaymentsRepository paymentsRepository,
            MastercardSession session) {
        this.ordersRepository = ordersRepository;
//        this.paymentsRepository = paymentsRepository;
        this.session = session;
    }

//	@ApiIgnore
//    @GetMapping(value = "/test/payment",produces=MediaType.TEXT_HTML_VALUE)
//    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") String orderList) throws BusinessException {
//        String initResult = initPayment(orderList).getBody().toString();
//        return new ResponseEntity<>(paymentService.getConfiguredHtml(initResult, "static/session.html", account), HttpStatus.OK);
//    }

    @ApiIgnore
    @GetMapping(value = "test/lightbox",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testLightbox(@RequestParam(name = "order_id") String orderList) throws BusinessException {
        String initResult = initPayment(orderList).getBody().toString();
        return new ResponseEntity<>(HTMLConfigurer.getConfiguredHtml(initResult, "static/mastercard-lightbox.html"), HttpStatus.OK);
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
            session.execute(sessionId);
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
            session.verifyAndStore(orderId, resultIndicator);
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
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") String orderList) throws BusinessException {
        OrderSessionResponse response = new OrderSessionResponse();
        response.setSuccess(false);

        String accountName = Tools.getAccount(Tools.getOrdersFromString(ordersRepository, orderList, ","), "mcard", orgPaymentGatewaysRep);

        Properties props = Tools.getPropertyForAccount(accountName, mastercardLogger, config.paymentPropertiesDir);
        if (props == null) {
            throw new BusinessException("Unknown payment account","",HttpStatus.NOT_ACCEPTABLE);
        }
        session.getMerchantAccount().init(props);
        mastercardLogger.info("Setting up payment for order(s): {} via processor: {}", orderList, session.getMerchantAccount().getMerchantId());

        if (session.initialize(Tools.getOrdersFromString(ordersRepository, orderList, ","))) {
            try {
                response.setOrderRef(session.getOrderRef());
                response.setOrderRef(session.getOrderRef());
                response.setSessionId(session.getSessionId());
                response.setMerchantId(session.getMerchantId());
                response.setApiUrl(session.getMerchantAccount().getApiUrl());
                response.setScriptUrl(session.getMerchantAccount().getScriptUrl());
                response.setSuccessUrl("/payment/mcard/verify");

                if (session.getOrderValue() != null) {
                    response.setOrderCurrency(session.getOrderValue().currency);
                    response.setOrderAmount(session.getOrderValue().amount);
                }
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
