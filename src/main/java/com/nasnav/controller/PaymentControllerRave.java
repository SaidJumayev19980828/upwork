package com.nasnav.controller;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.HTMLConfigurer;
import com.nasnav.payments.mastercard.MastercardSession;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.rave.RaveAccount;
import com.nasnav.persistence.OrdersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/rave")
public class PaymentControllerRave {

    private static final Logger reveLogger = LogManager.getLogger("Payment:REVE");

    private final OrdersRepository ordersRepository;

    private final PaymentsRepository paymentsRepository;

    @Autowired
    private AppConfig config;

    private RaveAccount account;

    @Autowired
    public PaymentControllerRave(
            OrdersRepository ordersRepository,
            PaymentsRepository paymentsRepository,
            MastercardSession session) {
        this.ordersRepository = ordersRepository;
        this.paymentsRepository = paymentsRepository;
        this.account = new RaveAccount(Tools.getPropertyForAccount("rave", reveLogger, "/"));
    }

    @ApiIgnore
    @GetMapping(value = "/test/payment",produces= MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        String initResult = initPayment(orderId).getBody().toString();
        return new ResponseEntity<>(HTMLConfigurer.getConfiguredHtml(initResult, "static/rave.html"), HttpStatus.OK);
    }

    @RequestMapping(value = "/initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        Optional<OrdersEntity> orderOpt = ordersRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new BusinessException("No order exists with that id", "", HttpStatus.NOT_ACCEPTABLE);
        }

        // some dummy data for initial tests
        OrdersEntity order = orderOpt.get();

        JSONObject data = new JSONObject();
        data.put("order_amount", "3500");
        data.put("order_currency", "NGN");
        data.put("order_id", "12-1341243134");
        data.put("public_key", account.getPublicKey());
        data.put("success_url", account.getSuccessUrl());
        data.put("failure_url", account.getFailureUrl());

        return new ResponseEntity<>(data.toString(), HttpStatus.OK);

//        throw new BusinessException("Unable to initialize RAVE payment session",null,HttpStatus.BAD_GATEWAY);
    }

 }
