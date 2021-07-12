package com.nasnav.payments.mastercard;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentRefundsRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Gateway;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.PaymentRefundEntity;
import com.nasnav.service.OrderService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class MastercardService {

    public static final int FLAVOR_BASIC = 1;
    public static final int FLAVOR_NBE = 2;

    private Logger classLogger = LogManager.getLogger("Payment:MCARD");

    @Autowired
    private AppConfig config;

    @Autowired
    private Commons paymentCommons;

    private final OrderService orderService;

    private final PaymentsRepository paymentsRepository;

    @Autowired
    private PaymentRefundsRepository paymentRefundsRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;


    public MastercardService(OrderService orderService, PaymentsRepository paymentsRepository) {
        this.orderService = orderService;
        this.paymentsRepository = paymentsRepository;
    }

    private String readInputStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public MastercardAccount getMerchantAccount(int orgPaymentId) throws BusinessException {
        try {
            MastercardAccount merchantAccount = new MastercardAccount();
            OrganizationPaymentGatewaysEntity gateway = orgPaymentGatewaysRep.getOne(orgPaymentId);
            merchantAccount.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir), orgPaymentId);
            return merchantAccount;
        } catch (Exception ex) {
            throw new BusinessException("Unable to find payment account data for org_payment_id = " + orgPaymentId, "INVALID_PAYMENT_ID", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public void execute(String paymentIdStr) throws BusinessException {
        PaymentEntity payment = null;
        try {
            long paymentId = Long.parseLong(paymentIdStr);
            Optional<PaymentEntity> op = paymentsRepository.findById(paymentId);
            if (op.isPresent()) {
                payment = op.get();
            }
        } catch ( Exception ex ) {
            throw new BusinessException("Invalid or empty payment ID", "INVALID_PAYMENT_ID", HttpStatus.NOT_ACCEPTABLE);
        }
        if (payment == null) {
            throw new BusinessException("Invalid or empty payment ID", "INVALID_PAYMENT_ID", HttpStatus.NOT_ACCEPTABLE);
        }
        String transactionId = "PAY-" + payment.getUid();

        MastercardAccount merchantAccount = getMerchantAccount(payment.getOrgPaymentId());

        try {
            // Prepare the request
            HttpClient client= HttpClientBuilder.create().build();
            HttpPut request = new HttpPut(merchantAccount.getApiUrl() + "/merchant/"
                    + merchantAccount.getMerchantId() + "/order/" + payment.getUid() + "/transaction/" + transactionId);

            // Set up the payload
            JSONObject orderObj = new JSONObject();
            orderObj.put("currency", payment.getCurrency());
            orderObj.put("amount", payment.getAmount());

            JSONObject sessionObj = new JSONObject();
            sessionObj.put("id", payment.getSessionId());

            JSONObject data = new JSONObject();
            data.put("apiOperation", "PAY");
            data.put("order", orderObj);
            data.put("session", sessionObj);

            // Execute call and fetch the result
            StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            classLogger.info("Payment execution, entity: {}", data );
            request.setHeader("Authorization", "Basic " + getAuthString(merchantAccount));
            HttpResponse response = client.execute(request);

            // Process the result
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                String errorResponse = readInputStream(response.getEntity().getContent());
                classLogger.error("Attempt to execute payment for order {} failed. Error provided: {}", payment.getUid(), errorResponse);
                throw new BusinessException("Unable to communicate with payment gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
            }

            String responseReceived = readInputStream(response.getEntity().getContent());

            payment.setObject(responseReceived);
            payment.setUid(transactionId);
            payment.setExecuted(new Date());
            // disable current session as it is used up
            payment.setSessionId(null);

            // Parse the response
            try {
                JSONObject jsonResult = new JSONObject(responseReceived);
                String result = jsonResult.getString("result");
                if (result.equalsIgnoreCase("SUCCESS")) {
                    // payment successful
                    payment.setStatus(PaymentStatus.PAID);
                    paymentsRepository.saveAndFlush(payment);
                    for (OrdersEntity order: orderService.getOrdersForMetaOrder(payment.getMetaOrderId())) {
                        classLogger.info("Payment successful for order: {}, payment ID: {}", order.getId(), payment.getId());
                        ordersRepository.setPaymentStatusForOrder(order.getId(), PaymentStatus.PAID.getValue(), new Date());
                    }
                } else {
                    // payment unsuccessful
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentsRepository.saveAndFlush(payment);
                    classLogger.info("Payment failed for order: {}, payment ID: {}", payment.getUid(), payment.getId());
                    throw new BusinessException("Payment failed", "PAYMENT_FAILED", HttpStatus.PAYMENT_REQUIRED);
                }
            } catch (JSONException ex) {
                payment.setStatus(PaymentStatus.ERROR);
                classLogger.error("Unable to process the response received (not a JSON): {}", responseReceived);
                throw new BusinessException("Unable to communicate with payment gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
            }
        } catch (IOException ex) {
            classLogger.error("Unable to execute payment", ex);
            throw new BusinessException("Unable to execute payment", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void verifyAndStore(String orderUid, String paymentIndicator) throws BusinessException {

        Optional<PaymentEntity> paymentOpt = paymentsRepository.findByUid(orderUid);
        if (!paymentOpt.isPresent()) {
            classLogger.warn("No payment associated with order {}", orderUid);
            throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
        }
        PaymentEntity payment = paymentOpt.get();
        JSONObject json = new JSONObject(payment.getObject());
        if (!json.has("successIndicator")) {
            classLogger.error("Payment {} for order {} does not contain successIndicator!", payment.getId(), orderUid);
            throw new BusinessException("Payment for order does not contain successIndicator", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (new Date().getTime() - payment.getExecuted().getTime() > 300000) {  // limit confirmation to 5 minutes
            classLogger.error("Attempt to confirm payment initiated on {}", payment.getExecuted());
            throw new BusinessException("Payment confirmation too late after initiation - more than 5 minutes", "REQUEST_TIMEOUT", HttpStatus.REQUEST_TIMEOUT);
        }
        if (payment.getStatus() != PaymentStatus.STARTED) {
            classLogger.error("Invalid state ({}) for payment {}", payment.getStatus(), payment.getId());
            throw new BusinessException("Invalid state for the payment ", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
        }
        if (json.getString("successIndicator").equals(paymentIndicator)) {
            payment.setExecuted(new Date());
            payment.setStatus(PaymentStatus.PAID);
            payment.setObject(json.toString());
            paymentCommons.finalizePayment(payment);
            return;
        }
        throw new BusinessException("Provided payment code does not match successIndicator", "INTVALID_CODE", CONFLICT);
    }
    

    public PaymentEntity initialize(MastercardAccount merchantAccount, Long metaOrderId) throws BusinessException {

        int flavor = merchantAccount.getFlavor();
        ArrayList<OrdersEntity> orders = orderService.getOrdersForMetaOrder(metaOrderId);

    	if(Objects.isNull(orders)) {
    		throw new BusinessException("No orders provided for payment!", "INVALID PARAM: order_id", NOT_ACCEPTABLE);
    	}    	

        String orderUid = Tools.getOrderUid(metaOrderId, classLogger);
        OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderId);
        if (orderValue == null) {
            throw new BusinessException("Order ID value is invalid", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
        }

        long userId = orders.get(0).getUserId();
        String responseContent = null;
        StringEntity requestEntity;

        if (flavor == FLAVOR_NBE) {
            String alahlyRequest = "apiOperation=CREATE_CHECKOUT_SESSION"
                    + "&apiPassword=" + merchantAccount.getApiPassword()
                    + "&apiUsername=" + merchantAccount.getApiUsername()
                    + "&merchant=" + merchantAccount.getMerchantId()
                    + "&interaction.operation=PURCHASE"
                    + "&order.id=" + orderUid
                    + "&order.amount=" + orderValue.amount
                    + "&order.currency=" + orderValue.currency;
            requestEntity = new StringEntity(alahlyRequest, ContentType.APPLICATION_FORM_URLENCODED);
        } else {
            JSONObject order = new JSONObject();
            order.put("id", orderUid);
            order.put("currency", orderValue.currency);
            order.put("amount", orderValue.amount);

            JSONObject interaction = new JSONObject();
            interaction.put("operation", "PURCHASE");

            JSONObject data = new JSONObject();
            data.put("apiOperation", "CREATE_CHECKOUT_SESSION");
            data.put("order", order);
            data.put("interaction", interaction);
            requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
        }

        try {
            HttpClient client= HttpClientBuilder.create().build();
            String sessionUrl = merchantAccount.getApiUrl() + "/merchant/" + merchantAccount.getMerchantId() +"/session";
            if (flavor == FLAVOR_NBE) {
                // Al-Ahly uses different URL schema
                sessionUrl = merchantAccount.getApiUrl();
            }
            HttpPost request = new HttpPost(sessionUrl);
            request.setEntity(requestEntity);
            request.setHeader("Authorization", "Basic " + getAuthString(merchantAccount));
            classLogger.debug("Attempting to call: " + request.getURI().toString()) ;
            HttpResponse response = client.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                String errorResponse = readInputStream(response.getEntity().getContent());
                classLogger.error("Attempt to set up hosted session resulted in {}. Error provided: {}", status, errorResponse);
                return null;
            }
            responseContent = readInputStream(response.getEntity().getContent());

            String result = "";
            String updateStatus = "";
            String sessionId = "";
            String indicator = "";
            if (flavor == FLAVOR_NBE) {
                String[] pairs = responseContent.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    String param = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    if ("result".equals(param)) {
                        result = value;
                    } else if ("session.updateStatus".equals(param)) {
                        updateStatus = value;
                    } else if ("session.id".equals(param)) {
                        sessionId = value;
                    } else if ("successIndicator".equals(param)) {
                        indicator = value;
                    }
                }
            } else {
                JSONObject jsonResult = new JSONObject(responseContent);
                result = jsonResult.getString("result");
                indicator = jsonResult.getString("successIndicator");
                JSONObject jsonSession = jsonResult.getJSONObject("session");
                updateStatus = jsonSession.getString("updateStatus");
                sessionId = jsonSession.getString("id");
            }
            if ("SUCCESS".equalsIgnoreCase(result) && "SUCCESS".equalsIgnoreCase(updateStatus)) {
                PaymentEntity payment = new PaymentEntity();
                payment.setOperator(merchantAccount.getAccountId());
                payment.setUid(orderUid);
                payment.setExecuted(new Date());
                payment.setStatus(PaymentStatus.STARTED);
                payment.setAmount(orderValue.amount);
                payment.setCurrency(orderValue.currency);
                payment.setObject("{\"successIndicator\": \"" + indicator + "\"}");
                payment.setSessionId(sessionId);
                payment.setUserId(userId);
                payment.setMetaOrderId(metaOrderId);
                payment.setOrgPaymentId(merchantAccount.getDbId());
                paymentsRepository.saveAndFlush(payment);
                return payment;
            }
            classLogger.error("Unable to set up hosted session, response: {}", responseContent);

        } catch (Exception ex) {
            classLogger.error("Unable to set up hosted session", ex);
        }
        return null;
    }

    public boolean refundTransaction(PaymentEntity payment, OrderService.OrderValue orderValue) throws BusinessException {

        String transactionUid = "" + payment.getId() + "-" + new Date().getTime();

        MastercardAccount merchantAccount = getAccountForOrder(payment.getMetaOrderId());
            if (orderValue == null) {
                // if not provided, refund the original amount
                orderValue = new OrderService.OrderValue();
                orderValue.currency = payment.getCurrency();
                orderValue.amount = payment.getAmount();
            }
            // Make sure we do not refund more than originally paid
            BigDecimal alreadyRefunded = new BigDecimal(0);
            List<PaymentRefundEntity> refunds = paymentRefundsRepository.findAllByPaymentEntity(payment);
            if (refunds != null) {
                for (PaymentRefundEntity refund: refunds) {
                    alreadyRefunded = alreadyRefunded.add(refund.getAmount());
                }
            }

            if (orderValue.amount.compareTo(payment.getAmount()) > 0) {
                classLogger.error("Attempt to execute refund for amount ({}), which is larger than the initial amount ({}) registered in payment ({})", orderValue.amount, payment.getAmount(), payment.getId());
                throw new BusinessException("Refund amount higher than payment", "REFUND_FAILED", NOT_ACCEPTABLE);
            }
            if (orderValue.amount.compareTo(payment.getAmount().subtract(alreadyRefunded)) > 0) {
                classLogger.error("Attempt to execute refund for amount ({}), which exceeds what remains after after already refunded ({})", orderValue.amount, alreadyRefunded);
                throw new BusinessException("Refund amount higher than payment", "REFUND_FAILED", NOT_ACCEPTABLE);
            }
            if (orderValue.currency != payment.getCurrency()) {
                classLogger.error("Requested refund currency ({}) differs from original payment currency ({})", orderValue.currency.name(), payment.getCurrency().name());
                throw new BusinessException("Refund currency doesn't match payment", "REFUND_FAILED", NOT_ACCEPTABLE);
            }

            PaymentRefundEntity refund = new PaymentRefundEntity();
            refund.setAmount(orderValue.amount);
            refund.setCurrency(orderValue.currency);
            refund.setPaymentEntity(payment);
            refund.setExecuted(new Date());
            refund.setStatus(PaymentStatus.STARTED);
            refund.setUid(transactionUid);
            paymentRefundsRepository.saveAndFlush(refund);

        try {
            HttpClient client= HttpClientBuilder.create().build();
            HttpPut request = new HttpPut(merchantAccount.getApiUrl()
                    + "/merchant/" + merchantAccount.getMerchantId()
                    + "/order/" + payment.getUid()
                    + "/transaction/" + transactionUid);

            JSONObject transactionObj = new JSONObject();
            transactionObj.put("amount", orderValue.amount);
            transactionObj.put("currency", orderValue.currency.name());

            JSONObject data = new JSONObject();
            data.put("apiOperation", "REFUND");
            data.put("transaction", transactionObj);

System.out.println("URI: " + request.getURI().toString());
System.out.println("PAYLOAD: " + data.toString());

            // Execute call and fetch the result
            StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            request.setHeader("Authorization", "Basic " + getAuthString(merchantAccount));
            HttpResponse response = client.execute(request);

            // Process the result
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                refund.setStatus(PaymentStatus.FAILED);
                paymentRefundsRepository.saveAndFlush(refund);
                String errorResponse = readInputStream(response.getEntity().getContent());
                classLogger.error("Attempt to execute refund for payment {} failed. Error provided: {}", payment.getId(), errorResponse);
                throw new BusinessException("Refund attempt failed", "REFUND_FAILED", NOT_ACCEPTABLE);
            }
            refund.setStatus(PaymentStatus.REFUNDED);
            paymentRefundsRepository.saveAndFlush(refund);
            return true;

        } catch (IOException ex) {
            classLogger.error("Unable to communicate with mastercard gateway", ex);
            throw new BusinessException("Unable to contact payment gateway", "", HttpStatus.BAD_GATEWAY);
        }
    }

    private String getAuthString(MastercardAccount merchantAccount) {
        String authString = "merchant."+merchantAccount.getMerchantId()
                + ":" + merchantAccount.getApiPassword();
        return new String(Base64.encodeBase64(authString.getBytes()));

    }

    public MastercardAccount getAccountForOrder(long metaOrderId) throws BusinessException {
        MastercardAccount merchantAccount = (MastercardAccount)paymentCommons.getMerchantAccount(metaOrderId, Gateway.MASTERCARD);
        if (merchantAccount == null) {
            classLogger.warn("Unable to find payment account for meta order {} via gateway: mcard", metaOrderId);
            throw new BusinessException("Unable to identify payment gateway","",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        classLogger.info("Setting up payment for meta order: {} via processor: {}", metaOrderId, merchantAccount.getMerchantId());
        return merchantAccount;
    }

}
