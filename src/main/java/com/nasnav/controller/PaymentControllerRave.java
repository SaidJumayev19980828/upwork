package com.nasnav.controller;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.HTMLConfigurer;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.rave.RaveAccount;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/rave")
public class PaymentControllerRave {

    private static final Logger reveLogger = LogManager.getLogger("Payment:RAVE");

    private final OrderService orderService;

    private final OrdersRepository ordersRepository;

    private final PaymentsRepository paymentsRepository;

    @Autowired
    private AppConfig config;

    private RaveAccount account;

    @Autowired
    public PaymentControllerRave(
            OrdersRepository ordersRepository,
            OrderService orderService,
            PaymentsRepository paymentsRepository) {
        this.ordersRepository = ordersRepository;
        this.orderService = orderService;
        this.paymentsRepository = paymentsRepository;
    }

    @ApiIgnore
    @GetMapping(value = "/test/payment",produces= MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        String initResult = initPayment(metaOrderId).getBody().toString();
        return new ResponseEntity<>(HTMLConfigurer.getConfiguredHtml(initResult, "static/rave.html"), HttpStatus.OK);
    }


    @GetMapping(value = "/success")
    public ResponseEntity<?> paymentSuccess(@RequestParam(name = "ref") String flwRef) throws BusinessException {

        if (this.account == null) {
            return  new ResponseEntity<>("{\"status\": \"FAILED\"}", HttpStatus.NOT_ACCEPTABLE);
        }
        // Verify transaction at the source
        JSONObject data = new JSONObject();
//        data.put("txref", this.transactionData.transactionId);
        data.put("flwref", flwRef);
        data.put("SECKEY", this.account.getPrivateKey());
        JSONObject responseObject = null;

        try {

            HttpClient client= HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(this.account.getApiUrl() + "/verify");
            StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            //System.out.println(request.getURI());
            HttpResponse response = client.execute(request);
//            int httpStatus = response.getStatusLine().getStatusCode();
            responseObject = new JSONObject(readInputStream(response.getEntity().getContent()));
//System.out.println("RESPONSE: " + responseObject.toString());
        } catch (IOException e) {
            reveLogger.error("Empty response for flwRef ({})", flwRef);
            throw new BusinessException("Unable to retrieve confirmation from the gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
        }

        if(responseObject == null || responseObject.isEmpty()) {
            reveLogger.error("Empty response for flwRef ({})", flwRef);
            throw new BusinessException("\"Unable to retrieve confirmation from the gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
        }

        String status = responseObject.optString("status", null);
        data = responseObject.getJSONObject("data");
        if(status == null || data == null) {
            reveLogger.error("Invalid response for flwRef ({}). JSON = {}", flwRef, responseObject);
            throw new BusinessException("Invalid state for the payment ", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
        }

        String orderUid = data.optString("txref", null);
        BigDecimal actualAmount = data.getBigDecimal("amount");
        if(orderUid == null || orderUid.isEmpty()) {
            reveLogger.error("Payment with flwRef ({}) doesn't have order uid/reference. JSON = {}", flwRef, responseObject);
            throw new BusinessException("Unable to identify order", "PAYMENT_FAILED", HttpStatus.BAD_GATEWAY);
        }

        Optional<PaymentEntity> paymentOpt = paymentsRepository.findByUid(orderUid);
        if (!paymentOpt.isPresent()) {
            reveLogger.warn("No payment associated with order {}", orderUid);
            throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
        }
        PaymentEntity payment = paymentOpt.get();
        payment.setObject(responseObject.toString());
        payment.setExecuted(new Date());

        if(!"success".equalsIgnoreCase(status)) {
            reveLogger.error("Payment failed for order {}", orderUid);
            payment.setStatus(PaymentStatus.FAILED);
            paymentsRepository.saveAndFlush(payment);
            throw new BusinessException("Invalid state for the payment ", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
        }
//System.out.println(" Actual: " +  actualAmount + " payment: " + payment.getAmount());
        if(payment.getAmount() == null || actualAmount == null || actualAmount.compareTo(payment.getAmount()) != 0) {
//                actualAmount.movePointRight(2).intValue() != payment.getAmount().movePointRight(2).intValue()) {
            reveLogger.error("Payment amount doesn't match order {}", orderUid);
            payment.setStatus(PaymentStatus.FAILED);
            paymentsRepository.saveAndFlush(payment);
            throw new BusinessException("Invalid payment amount", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
        }

        // Everything seems OK
        for (OrdersEntity order: ordersRepository.findByPaymentEntity_idOrderById(payment.getId())) {
            orderService.setOrderAsPaid(payment, order);
        }
        ordersRepository.flush();

        payment.setStatus(PaymentStatus.PAID);
        payment.setUid("CFM-" + payment.getUid());
        paymentsRepository.saveAndFlush(payment);
// #FINALIZE            orderService.finalizeOrder(payment.getMetaOrderId());

        return  new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);
    }

    private String readInputStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
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
        ArrayList<OrdersEntity> orders = orderService.getOrdersForMetaOrder(metaOrderId);

        // Load the appropriate receiver account. TODO: based on the order data
        this.account = new RaveAccount(Tools.getPropertyForAccount("rave", reveLogger, config.paymentPropertiesDir));
        reveLogger.debug("Payment Account: {}, API: {}", this.account.getAccountId(), this.account.getApiUrl());

        if (orders.size() == 0) {
            reveLogger.error("No sub-orders matching meta order ({})", metaOrderId);
            throw new BusinessException("No valid order IDs recognized", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
        }
        OrderService.OrderValue orderValue = Tools.getTotalOrderValue(orders, this.orderService, reveLogger);

// TODO
        orderValue.currency = TransactionCurrency.NGN;

        if (orderValue.currency != TransactionCurrency.NGN) {
            reveLogger.error("Payment for meta order ({}) failed due to incorrect currency: {}", metaOrderId, orderValue.currency);
            throw new BusinessException("Invalid currency, this gateway only supports NGN", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
        }

        String transactionId = Tools.getOrderUid(metaOrderId, reveLogger);

        JSONObject data = new JSONObject();
        data.put("order_amount", orderValue.amount);
        data.put("order_currency", orderValue.currency);
        data.put("order_id", transactionId);
        data.put("public_key", account.getPublicKey());
        data.put("script_url", account.getScriptUrl());
        data.put("success_url", account.getSuccessUrl());
        data.put("failure_url", account.getFailureUrl());

        PaymentEntity payment = new PaymentEntity();
        payment.setStatus(PaymentStatus.STARTED);
        payment.setAmount(orderValue.amount);
        payment.setCurrency(orderValue.currency);
        payment.setUid(transactionId);
        payment.setOperator(account.getAccountId());
        payment.setExecuted(new Date());
        payment.setUserId(orders.get(0).getUserId());
        payment.setMetaOrderId(metaOrderId);
        paymentsRepository.saveAndFlush(payment);

        for (OrdersEntity order: orders) {
            order.setPaymentEntity(payment);
            ordersRepository.save(order);
        }
        ordersRepository.flush();

        return new ResponseEntity<>(data.toString(), HttpStatus.OK);

//        throw new BusinessException("Unable to initialize RAVE payment session",null,HttpStatus.BAD_GATEWAY);
    }

 }
