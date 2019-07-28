package com.nasnav.payments.qnb;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.OrderService;
import lombok.Setter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nasnav.enumerations.TransactionCurrency.UNSPECIFIED;

@Service
public class Session {

    private static final Logger qnbLogger = LogManager.getLogger("Payment:QNB");
    private static final String paymentOperator = "QNB";

    private final OrderService orderService;

    private final PaymentsRepository paymentsRepository;

    private final OrdersRepository ordersRepository;

    @Setter
    Account merchantAccount;

    private String sessionId;
    private String orderUid;

    private OrdersEntity order = null;
    private OrderService.OrderValue orderValue = null;

    @Autowired
    public Session(Account account, OrderService orderService, PaymentsRepository paymentsRepository, OrdersRepository ordersRepository) {
        this.merchantAccount = account;
        this.orderService = orderService;
        this.paymentsRepository = paymentsRepository;
        this.ordersRepository = ordersRepository;
    }

    private String readInputStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public void execute(String sessionId) throws BusinessException {
        if (sessionId == null || !sessionId.equals(this.sessionId)) {
            throw new BusinessException("Invalid or empty session ID", "MISSING_SESSION_ID", HttpStatus.NOT_ACCEPTABLE);
        }
        if (this.order == null || this.orderValue == null) {
            throw new BusinessException("Order not assigned to session", "MISSING_ORDER", HttpStatus.NOT_ACCEPTABLE);
        }
        String transactionId = "PAY-" + order.getId() + "-" + Long.toString(new Date().getTime());
        try {
            // Prepare the request
            HttpClient client= HttpClientBuilder.create().build();
            HttpPut request = new HttpPut(merchantAccount.getApiUrl() + merchantAccount.getApiVersion() + "/merchant/"
                    + merchantAccount.getMerchantId() + "/order/" + this.orderUid + "/transaction/" + transactionId);

            // Set up the payload
            JSONObject orderObj = new JSONObject();
            orderObj.put("currency", orderValue.currency);
            orderObj.put("amount", orderValue.amount);

            JSONObject sessionObj = new JSONObject();
            sessionObj.put("id", this.sessionId);

            JSONObject data = new JSONObject();
            data.put("apiOperation", "PAY");
            data.put("order", orderObj);
            data.put("session", sessionObj);

            // Execute call and fetch the result
            StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            request.setHeader("Authorization", "Basic " + getAuthString());
            HttpResponse response = client.execute(request);

            // Process the result
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                String errorResponse = readInputStream(response.getEntity().getContent());
                qnbLogger.error("Attempt to execute payment for order {} failed. Error provided: {}", order.getId(), errorResponse);
                throw new BusinessException("Unable to communicate with payment gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
            }

            String responseReceived = readInputStream(response.getEntity().getContent());
            this.sessionId = null; // disable current session as it is used up

            List<PaymentEntity> paymentOpt = paymentsRepository.findRecentByOrdersEntity_Id(this.order.getId());
            PaymentEntity payment = paymentOpt.size() > 0 ? paymentOpt.get(0) : new PaymentEntity();
            payment.setOperator(paymentOperator);
            payment.setObject(responseReceived);
            payment.setOrdersEntity(this.order);
            payment.setUid(transactionId);
            payment.setExecuted(new Date());
            payment.setAmount(this.orderValue.amount);
            payment.setCurrency(this.orderValue.currency);

            // Parse the response
            try {
                JSONObject jsonResult = new JSONObject(responseReceived);
                String result = jsonResult.getString("result");
                if (result.equalsIgnoreCase("SUCCESS")) {
                    // payment successful
                    payment.setStatus(PaymentStatus.PAID);
                    paymentsRepository.saveAndFlush(payment);
                    qnbLogger.info("Payment successful for order: {}, payment ID: {}", order.getId(), payment.getId());
                    ordersRepository.setPaymentStatusForOrder(this.order.getId(), PaymentStatus.PAID.getValue(), new Date());
                } else {
                    // payment unsuccessful
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentsRepository.saveAndFlush(payment);
                    qnbLogger.info("Payment failed for order: {}, payment ID: {}", order.getId(), payment.getId());
                    throw new BusinessException("Payment failed", "PAYMENT_FAILED", HttpStatus.PAYMENT_REQUIRED);
                }
            } catch (JSONException ex) {
                payment.setStatus(PaymentStatus.ERROR);
                qnbLogger.error("Unable to process the response received (not a JSON): {}", responseReceived);
                throw new BusinessException("Unable to communicate with payment gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
            }
        } catch (IOException ex) {
            qnbLogger.error("Unable to execute payment", ex);
            throw new BusinessException("Unable to execute payment", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean verifyAndStore(String orderUid, String paymentIndicator) throws BusinessException {

        Optional<PaymentEntity> paymentOpt = paymentsRepository.findByUid(orderUid);
        if (!paymentOpt.isPresent()) {
            qnbLogger.warn("No payment associated with order {}", this.order.getId());
            throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
        }
        PaymentEntity payment = paymentOpt.get();
        JSONObject json = new JSONObject(payment.getObject());
        if (!json.has("successIndicator")) {
            qnbLogger.error("Payment {} for order {} does not contain successIndicator!", payment.getId(), this.order.getId());
            throw new BusinessException("Payment for order does not contain successIndicator", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (new Date().getTime() - payment.getExecuted().getTime() > 300000) {  // limit confirmation to 5 minutes
            qnbLogger.error("Attempt to confirm payment initiated on {}", payment.getExecuted());
            throw new BusinessException("Payment confirmation too late after initiation - more than 5 minutes", "REQUEST_TIMEOUT", HttpStatus.REQUEST_TIMEOUT);
        }
        if (payment.getStatus() != PaymentStatus.STARTED) {
            qnbLogger.error("Invalid state ({}) for payment {}", payment.getStatus(), payment.getId());
            throw new BusinessException("Invalid state for the payment ", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
        }
        if (json.getString("successIndicator").equals(paymentIndicator)) {
//            payment.setOrdersEntity(this.order);
            payment.setUid("CFM-" + payment.getUid());
            payment.setExecuted(new Date());
            payment.setStatus(PaymentStatus.PAID);
            paymentsRepository.saveAndFlush(payment);
            ordersRepository.setPaymentStatusForOrder(this.order.getId(), PaymentStatus.PAID.getValue(), payment.getExecuted());
            return true;
        }
        throw new BusinessException("Provided payment code does not match successIndicator", "INTVALID_CODE", HttpStatus.CONFLICT);
    }

    public boolean initialize(OrdersEntity orderEntity) throws BusinessException {

        this.order = orderEntity;
        if (this.order == null) {
            throw new BusinessException("Order does not exist", "MISSING_ORDER", HttpStatus.NOT_ACCEPTABLE);
        }

        this.orderValue = orderService.getOrderValue(this.order);
        if (this.orderValue.currency == UNSPECIFIED) {
            qnbLogger.error("Cannot process order {} which contains items in different currencies", order.getId());
            throw new BusinessException("Cannot process order which contains items in different currencies", "ERROR_MIXED_CURRENCIES", HttpStatus.NOT_ACCEPTABLE);
        }

        JSONObject order = new JSONObject();
        orderUid = Long.toString(orderEntity.getId()) + "-" + Long.toString(new Date().getTime());
        order.put("id", orderUid);
        order.put("currency", orderValue.currency);
        order.put("amount", orderValue.amount);

        JSONObject interaction = new JSONObject();
        interaction.put("operation", "NONE");

        JSONObject data = new JSONObject();
        data.put("apiOperation", "CREATE_CHECKOUT_SESSION");
        data.put("order", order);
        data.put("interaction", interaction);

        try {
            HttpClient client= HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(merchantAccount.getApiUrl() + merchantAccount.getApiVersion()
                    + "/merchant/" + merchantAccount.getMerchantId() +"/session");
            StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            request.setHeader("Authorization", "Basic " + getAuthString());

            HttpResponse response = client.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                String errorResponse = readInputStream(response.getEntity().getContent());
                qnbLogger.error("Attempt to set up hosted session resulted in {}. Error provided: {}", status, errorResponse);
                return false;
            }
            JSONObject jsonResult = new JSONObject(readInputStream(response.getEntity().getContent()));
            String result = jsonResult.getString("result");
            String indicator = jsonResult.getString("successIndicator");
            JSONObject jsonSession = jsonResult.getJSONObject("session");
            String updateStatus = jsonSession.getString("updateStatus");
            sessionId = jsonSession.getString("id");
            if ("SUCCESS".equalsIgnoreCase(result) && "SUCCESS".equalsIgnoreCase(updateStatus)) {
                PaymentEntity payment = new PaymentEntity();
                payment.setOperator("QNB");
                payment.setOrdersEntity(this.order);
                payment.setUid(orderUid);
                payment.setExecuted(new Date());
                payment.setStatus(PaymentStatus.STARTED);
                payment.setAmount(orderValue.amount);
                payment.setCurrency(orderValue.currency);
                payment.setObject("{\"successIndicator\": \"" + indicator + "\"}");
                paymentsRepository.saveAndFlush(payment);
                return true;
            }
            qnbLogger.error("Unable to set up hosted session, response: {}", jsonResult.toString());

        } catch (IOException ex) {
            qnbLogger.error("Unable to set up hosted session", ex);
        }
        return false;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getOrderRef() {
        return this.orderUid;
    }

    private String getAuthString() {
        String authString = "merchant."+merchantAccount.getMerchantId()
                + ":" + merchantAccount.getApiPassword();
        return new String(Base64.encodeBase64(authString.getBytes()));

    }

    public String getMerchantId() {
        return this.merchantAccount == null ? null : this.merchantAccount.getMerchantId();
    }

    public OrderService.OrderValue getOrderValue() {
        return orderValue;
    }
}
