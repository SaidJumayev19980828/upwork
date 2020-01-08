package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misr.MisrAccount;
import com.nasnav.payments.misr.MisrSession;
import com.nasnav.payments.qnb.QnbAccount;
import com.nasnav.payments.mastercard.PaymentService;
import com.nasnav.payments.mastercard.Session;
import com.nasnav.persistence.OrdersEntity;
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

import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/misr")
public class MisrPaymentController {

    private static final Logger misrLogger = LogManager.getLogger("Payment:MISR");

    private final PaymentService paymentService;

    private final OrdersRepository ordersRepository;

    private final PaymentsRepository paymentsRepository;

    private final MisrSession session;

    private MisrAccount account;

    @Autowired
    public MisrPaymentController(
            PaymentService paymentService,
            OrdersRepository ordersRepository,
            PaymentsRepository paymentsRepository,
            MisrSession session) {
        this.paymentService = paymentService;
        this.ordersRepository = ordersRepository;
        this.paymentsRepository = paymentsRepository;
        this.session = session;
        this.account = new MisrAccount();
        account.setup();
    }

    @ApiIgnore
    @GetMapping(value = "/test/payment",produces= MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        String initResult = initPayment(orderId).getBody().toString();
        return new ResponseEntity<>(paymentService.getConfiguredHtml(initResult, "static/session.html", account), HttpStatus.OK);
    }

    @ApiIgnore
    @GetMapping(value = "/test/lightbox",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testLightbox(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        String initResult = initPayment(orderId).getBody().toString();
        return new ResponseEntity<>(paymentService.getConfiguredHtml(initResult, "static/misr-lightbox.html", account), HttpStatus.OK);
    }

    @ApiOperation(value = "Execute the payment after setup and user's data collection", nickname = "misrExecute")
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

    @ApiOperation(value = "Verify that the payment initiated via lightbox has successfully completed", nickname = "misrVerify")
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

    @ApiOperation(value = "Execute the payment after setup and user data collection", nickname = "misrExecute")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Payment session set up"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid input data"),
            @io.swagger.annotations.ApiResponse(code = 402, message = "Payment attempted but failed (refused by the gateway)"),
            @io.swagger.annotations.ApiResponse(code = 502, message = "Unable to communicate with the payment gateway"),
    })

    @PostMapping(value = "/initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        Optional<OrdersEntity> orderOpt = ordersRepository.findById(orderId);
        if(!orderOpt.isPresent()) {
            throw new BusinessException("No order exists with that id", null, HttpStatus.NOT_ACCEPTABLE);
        }
//        session.setMerchantAccount(account);
//        System.out.println("XXXX: " + session.getMerchantAccount().getMerchantId());
        OrdersEntity order = orderOpt.get();
        OrderSessionResponse response = new OrderSessionResponse();
        response.setSuccess(false);

        if (session.initialize(order)) {
            try {
                response.setOrderRef(session.getOrderRef());
                response.setOrderRef(session.getOrderRef());
                response.setSessionId(session.getSessionId());
                response.setMerchantId(session.getMerchantId());

                if (session.getOrderValue() != null) {
                    response.setOrderCurrency(session.getOrderValue().currency);
                    response.setOrderAmount(session.getOrderValue().amount);
                }
                response.setExecuteUrl("/payment/misr/execute");
                response.setSuccess(true);
            } catch(Exception ex){
                ex.printStackTrace();
            }
            ObjectMapper oMap = new ObjectMapper();
            String result = "{}";
            try {
                result = oMap.writeValueAsString(response);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        throw new BusinessException("Unable to initialize MISR payment session",null,HttpStatus.BAD_GATEWAY);
    }

 }
