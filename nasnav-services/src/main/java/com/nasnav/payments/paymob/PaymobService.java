package com.nasnav.payments.paymob;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.PaymobSourceRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Gateway;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.PaymobSourceEntity;
import com.nasnav.service.OrderService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class PaymobService {
    /* static final String BASE_URL = "https://accept.paymob.com/api";
     static final String api_key = "ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKSVV6VXhNaUo5LmV5SnVZVzFsSWpvaWFXNXBkR2xoYkNJc0ltTnNZWE56SWpvaVRXVnlZMmhoYm5RaUxDSndjbTltYVd4bFgzQnJJam94TkRJNU5EaDkubWFYelNWeDhvRjJoN2pEUTAyMHFSMUg1TE9OaFhJcm5LWE9wd2lKMkgybnotVXp0eWFYY0l5UF9yUV84cHJSUlZnRjlfQWVVUV8tMDdqMWJlMmZQaUE=";
 */
    private Logger classLogger = LogManager.getLogger("Payment:PAYMOB");

    @Autowired
    private Commons paymentCommons;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private PaymobSourceRepository paymobSourceRepository;

    private PayMobAccount payMobAccount;
    private HttpClient httpClient;

    public TokenResponse getAuthToken() throws BusinessException {
        TokenResponse tokenResponse = null;
        try {
            httpClient = getHttpClient();
            String tokenUrl = payMobAccount.getApiUrl() + "/auth/tokens";
            HttpPost post = new HttpPost(tokenUrl);
            post.addHeader("Content-Type", APPLICATION_JSON_VALUE);
            JSONObject param = new JSONObject();
            param.put("api_key", payMobAccount.getPrivateKey());
            post.setEntity(new StringEntity(param.toString()));
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                String resBody = readInputStream(response.getEntity().getContent());
                tokenResponse = new Gson().fromJson(resBody, TokenResponse.class);
            }
        } catch (Exception ex) {
            throw new BusinessException("Couldn't generate payment authentication", "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        }
        return tokenResponse;
    }

    private String readInputStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public OrderResponse registerOrder(@NotNull OrderRequest order) throws BusinessException {
        OrderResponse orderResponse = null;
        try {
            Gson gson = getGson();

            String orderURL = payMobAccount.getApiUrl() + "/ecommerce/orders";
            HttpPost post = new HttpPost(orderURL);
            post.setHeader("Content-Type", APPLICATION_JSON_VALUE);
            String body = gson.toJson(order);
            post.setEntity(new StringEntity(body));
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                String resBody = readInputStream(response.getEntity().getContent());
                orderResponse = gson.fromJson(resBody, OrderResponse.class);
            } else {
                throw new BusinessException(response.getEntity().getContent().toString(), "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception ex) {
            classLogger.error(ex);
            throw new BusinessException("Couldn't connect to payment gateway", "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        return orderResponse;

    }


    public String init(MetaOrderEntity metaOrder, PaymobSource source) throws BusinessException {

        long metaOrderId = metaOrder.getId();
        OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderId);

        if (orderValue == null) {
            throw new BusinessException("Order ID is invalid", "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        ArrayList<OrdersEntity> orders = orderService.getOrdersForMetaOrder(metaOrderId);

        if (Objects.isNull(orders)) {
            throw new BusinessException("No orders provided for payment!", "INVALID PARAM: order_id", NOT_ACCEPTABLE);
        }
        payMobAccount = getAccountForOrder(metaOrder.getId());

        Long userId = orders.get(0).getUserId();
        Long transactionId = Long.valueOf(Tools.getOrderUid(metaOrderId, classLogger).replace("-", ""));

        TokenResponse authToken = getAuthToken();
        TokenResponse paymentToken = null;

        PaymobSourceEntity sourceEntity = paymobSourceRepository.findByValue(source.getIdentifier()).orElseThrow(() -> new BusinessException("Payment source not found", "PAYMENT_FAILED", NOT_ACCEPTABLE));

        if (authToken != null) {
            HttpClient client = getHttpClient();
            OrderResponse orderResponse = registerOrder(fromOrderValue(orderValue, authToken.getToken(), transactionId));
            if (orderResponse != null) {
                paymentToken = getPaymentTokenResponse(authToken.getToken(), metaOrderId, orderValue, client, orderResponse, userId, sourceEntity);
            }
        }
        if (paymentToken != null) {
            if(!sourceEntity.getType().equalsIgnoreCase("CARD")) {
                return pay(paymentToken, authToken, metaOrder, sourceEntity);
            } else {
                return  "{\"token\":\""+paymentToken.getToken()+"\"}";
            }

        }

        throw new BusinessException("Couldn't generate payment", "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
     }

    private String pay(TokenResponse paymentToken, TokenResponse authToken, MetaOrderEntity metaOrder, PaymobSourceEntity sourceEntity) throws BusinessException {
        Gson gson = getGson();

        JSONObject sourceJson = new JSONObject();
        sourceJson.put("identifier", sourceEntity.getIdentifier());
        sourceJson.put("subtype", sourceEntity.getType());


        JSONObject data = new JSONObject();
        data.put("source", sourceJson);
        data.put("payment_token", paymentToken.getToken());
     //   data.put("billing_data", new JSONObject());



        String orderURL = payMobAccount.getApiUrl() + "/acceptance/payments/pay";
        HttpPost post = new HttpPost(orderURL);
        post.setHeader("Content-Type", APPLICATION_JSON_VALUE);
         try {
            post.setEntity(new StringEntity(data.toString(), ContentType.APPLICATION_JSON));
            classLogger.info("Payment execution, entity: {}", data.toString());

            HttpResponse response = httpClient.execute(post);
            // Process the result
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                String errorResponse = readInputStream(response.getEntity().getContent());
                classLogger.error("Attempt to execute payment for order {} failed. Error provided: {}", paymentToken.getToken(), errorResponse);
                throw new BusinessException(errorResponse, "PAYMENT_UNRECOGNIZED_RESPONSE", org.springframework.http.HttpStatus.BAD_GATEWAY);
            }
            String resBody = readInputStream(response.getEntity().getContent());
            return "{\"token\":\""+paymentToken.getToken()+"\", \"data\":"+resBody+"}";
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage(), "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private TokenResponse getPaymentTokenResponse(String authToken, long metaOrderId, OrderService.OrderValue orderValue, HttpClient client, OrderResponse orderResponse, Long userId, PaymobSourceEntity source) throws BusinessException {
        PaymentRequest paymentRequest = PaymentRequest.fromOrderResponse(orderResponse, source);
        paymentRequest.setAuth_token(authToken);
        TokenResponse paymentToken = null;

        JSONObject paymentJsonObject = new JSONObject();
        paymentJsonObject.put("auth_token", authToken);
        paymentJsonObject.put("amount_cents", orderResponse.getAmountCents());
        paymentJsonObject.put("expiration", 3600);
        paymentJsonObject.put("order_id", orderResponse.getId().toString());
        JSONObject billingDataJsonObject = new JSONObject();

        // TODO: add billing data
        billingDataJsonObject.put("apartment", "1");
        billingDataJsonObject.put("email", "email@email");
        billingDataJsonObject.put("floor", "1");
        billingDataJsonObject.put("first_name", "user");
        billingDataJsonObject.put("street", "street");
        billingDataJsonObject.put("building", "1");
        billingDataJsonObject.put("phone_number", "+21");
        billingDataJsonObject.put("shipping_method", "");
        billingDataJsonObject.put("postal_code", "12345");
        billingDataJsonObject.put("city", "Cairo");
        billingDataJsonObject.put("country", "Egypt");
        billingDataJsonObject.put("last_name", "user");
        billingDataJsonObject.put("state", "Cairo");

        paymentJsonObject.put("billing_data", billingDataJsonObject);
        paymentJsonObject.put("currency", orderValue.currency);
        paymentJsonObject.put("integration_id", source.getValue());
        paymentJsonObject.put("lock_order_when_paid", "false");
        try {
            String paymentKeyUrl = payMobAccount.getApiUrl() + "/acceptance/payment_keys";
            HttpPost post = new HttpPost(paymentKeyUrl);

            Gson gson = getGson();

            String body = gson.toJson(paymentRequest);
            post.setEntity(new StringEntity(paymentJsonObject.toString(), ContentType.APPLICATION_JSON));
            post.setHeader("Content-Type", APPLICATION_JSON_VALUE);
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                String resBody = readInputStream(response.getEntity().getContent());
                paymentToken = gson.fromJson(resBody, TokenResponse.class);

                createPaymentEntity(metaOrderId, orderValue, paymentToken, userId);
            } else {
                throw new BusinessException(readInputStream(response.getEntity().getContent()), "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage(), "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        return paymentToken;
    }


    private PaymentEntity createPaymentEntity(long metaOrderId, OrderService.OrderValue orderValue, TokenResponse paymentToken, Long userId) {
        PaymentEntity payment = new PaymentEntity();
        payment.setStatus(PaymentStatus.STARTED);
        payment.setAmount(orderValue.amount);
        payment.setCurrency(orderValue.currency);
        payment.setUid(paymentToken.getToken());
        payment.setOperator(payMobAccount.getAccountId());
        payment.setExecuted(new Date());
        payment.setUserId(userId);
        payment.setMetaOrderId(metaOrderId);
        paymentsRepository.saveAndFlush(payment);
        return payment;
    }

    private OrderRequest fromOrderValue(OrderService.OrderValue metaOrder, String token, Long transactionId) {
        return OrderRequest.builder()
                .authToken(token)
                .amountCents(metaOrder.amount.multiply(new BigDecimal(100)))
                .build();
    }

    private HttpClient getHttpClient() {
        return HttpClientBuilder.create().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
    }


    public PayMobAccount getAccountForOrder(long metaOrderId) throws BusinessException {
        PayMobAccount merchantAccount = (PayMobAccount) paymentCommons.getMerchantAccount(metaOrderId, Gateway.PAY_MOB);
        if (merchantAccount == null) {
            classLogger.warn("Unable to find payment account for meta order {} via gateway: paymob", metaOrderId);
            throw new BusinessException("Unable to identify payment gateway", "", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
        classLogger.info("Setting up payment for meta order: {}", metaOrderId);
        return merchantAccount;
    }

    public void verifyAndStore(String orderUid) throws BusinessException {
        PaymentEntity payment = paymentCommons.getPaymentForOrderUid(orderUid);
        if (payment == null) {
            classLogger.warn("No payment associated with order {}", orderUid);
            throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        long orderId = payment.getMetaOrderId();
        payMobAccount = getAccountForOrder(orderId);
        if (payMobAccount == null) {
            throw new BusinessException("No account associated with UID: " + orderUid, "INVALID_INPUT", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        if (payment.getStatus() != PaymentStatus.STARTED) {
            classLogger.error("Invalid state ({}) for payment {}", payment.getStatus(), payment.getId());
            throw new BusinessException("Invalid state for the payment ", "INVALID_INPUT", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        TokenResponse authToken = getAuthToken();

        String paymentDetailUrl = payMobAccount.getApiUrl() + "/acceptance/transactions/" + payment.getUid();
        HttpGet get = new HttpGet(paymentDetailUrl);
        get.addHeader("Content-Type", APPLICATION_JSON_VALUE);
        get.addHeader("authorization-header", "Bearer " + authToken.getToken());

        try {
            HttpResponse response = httpClient.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                Gson gson = getGson();
                String resBody = readInputStream(response.getEntity().getContent());
                RetrieveTransactionResponse status = gson.fromJson(resBody, RetrieveTransactionResponse.class);
                if (status != null) {
                    RetrieveTransactionResponse.PaymentDetails data = status.getObj();
                    if (!data.getSuccess()) {
                        return;
                    }
                    if (data.getIsAuth()) {
                        payment.setStatus(PaymentStatus.PAID);
                    } else if (data.getIsRefund()) {
                        payment.setStatus(PaymentStatus.REFUNDED);
                    } else if (data.getIsVoided()) {
                        payment.setStatus(PaymentStatus.UNPAID);
                    }
                    paymentCommons.finalizePayment(payment);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Couldn't connect to payment gateway", "PAYMENT_FAILED", org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }

    }

    private Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }
}
