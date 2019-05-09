package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.qnb.Account;
import com.nasnav.payments.qnb.PaymentService;
import com.nasnav.payments.qnb.Session;
import com.nasnav.persistence.OrdersEntity;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
@RequestMapping("/payment/qnb")
public class QnbPaymentController {

//    private final ActiveSessions activeSessions;
    
    private final PaymentService paymentService;

    private final OrderRepository orderRepository;

    private final Session session;

    @Autowired
    public QnbPaymentController(PaymentService paymentService, OrderRepository orderRepository, Session session) {
//        this.activeSessions = activeSessions;
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.session = session;
    }


    @RequestMapping(value = "/test/payment",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        String initResult = initPayment(orderId).getBody().toString();
        return new ResponseEntity<>(paymentService.getConfiguredHtml(initResult), HttpStatus.OK);
    }

    @ApiOperation(value = "Execute the payment after setup and user's data collection", nickname = "qnbExecute")
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

    @ApiOperation(value = "Execute the payment after setup and user data collectiob", nickname = "qnbExecute")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Payment session set up"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid input data"),
            @io.swagger.annotations.ApiResponse(code = 402, message = "Payment attempted but failed (refused by the gateway)"),
            @io.swagger.annotations.ApiResponse(code = 502, message = "Unable to communicate with the payment gateway"),
    })
    @PostMapping(value = "/initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        Optional<OrdersEntity> orderOpt = orderRepository.findById(orderId);
        if(!orderOpt.isPresent()) {
            throw new BusinessException("No order exists with that id", null, HttpStatus.NOT_ACCEPTABLE);
        }
        session.setMerchantAccount(new Account());
        OrdersEntity order = orderOpt.get();
        OrderSessionResponse response = new OrderSessionResponse();
        response.setSuccess(false);

        if (session.initialize(order)) {
            try {
                response.setOrderRef(session.getOrderRef());
                response.setOrderRef(session.getOrderRef());
                response.setSessionId(session.getSessionId());

                if (session.getOrderValue() != null) {
                    response.setOrderCurrency(session.getOrderValue().currency);
                    response.setOrderAmount(session.getOrderValue().amount);
                }
                response.setExecuteUrl("/payment/qnb/execute");
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

        throw new BusinessException("Unable to initialize QNB payment session",null,HttpStatus.BAD_GATEWAY);
    }

 }
