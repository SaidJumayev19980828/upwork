package com.nasnav.payments.paymob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.PaymobSourceRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Gateway;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.*;
import com.nasnav.service.OrderService;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.enumerations.PaymentStatus.*;
import static com.nasnav.enumerations.PaymobName.ONLINE_CARD;
import static com.nasnav.exceptions.ErrorCodes.PAYMENT$CALLBACK$001;
import static com.nasnav.exceptions.ErrorCodes.PAYMENT$CALLBACK$002;
import static org.springframework.http.HttpStatus.*;
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
    @Autowired
    private ObjectMapper mapper;
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
            throw new BusinessException("Couldn't generate payment authentication", "PAYMENT_FAILED", NOT_ACCEPTABLE);

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
                throw new BusinessException(response.getEntity().getContent().toString(), "PAYMENT_FAILED", NOT_ACCEPTABLE);
            }
        } catch (Exception ex) {
            classLogger.error(ex);
            throw new BusinessException("Couldn't connect to payment gateway", "PAYMENT_FAILED", NOT_ACCEPTABLE);
        }
        return orderResponse;

    }


    public LinkedHashMap<String, String> payMobCardInit(MetaOrderEntity metaOrder) throws BusinessException {

        long metaOrderId = metaOrder.getId();
        OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderId);

        if (orderValue == null) {
            throw new BusinessException("Order ID is invalid", "PAYMENT_FAILED", NOT_ACCEPTABLE);
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

        PaymobSourceEntity sourceEntity = paymobSourceRepository.findByValue(ONLINE_CARD.getValue()).orElseThrow(() -> new BusinessException("Payment source not found", "PAYMENT_FAILED", NOT_ACCEPTABLE));

        if (!Objects.isNull(authToken)) {
            HttpClient client = getHttpClient();
            OrderResponse orderResponse = registerOrder(fromOrderValue(orderValue, authToken.getToken(), transactionId));
            if (orderResponse != null) {
                paymentToken = getPaymentTokenResponse(authToken.getToken(), metaOrder, orderValue, client, orderResponse, userId, sourceEntity, transactionId);
            }
        }
        if (!Objects.isNull(paymentToken)) {
            if(!sourceEntity.getType().equalsIgnoreCase("CARD")) {
                return pay(paymentToken, sourceEntity);
            } else {
                return getInitJson(sourceEntity, paymentToken);
            }
        }

        throw new BusinessException("Couldn't generate payment", "PAYMENT_FAILED", NOT_ACCEPTABLE);
     }

    private LinkedHashMap<String, String> getInitJson(PaymobSourceEntity sourceEntity, TokenResponse paymentToken){
        LinkedHashMap<String, String> map = new LinkedHashMap();

        map.put("iframe_url", getIframeUrl(sourceEntity, paymentToken));
        map.put("token", paymentToken.getToken());

        return map;
    }

    private String getIframeUrl(PaymobSourceEntity sourceEntity, TokenResponse paymentToken) {
        StringBuilder iFrameUrl = new StringBuilder();
        iFrameUrl
                .append(sourceEntity.getScript())
                .append("?payment_token=")
                .append(paymentToken.getToken());

        return iFrameUrl.toString();
    }

    private LinkedHashMap<String, String> pay(TokenResponse paymentToken, PaymobSourceEntity sourceEntity) throws BusinessException {

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
            LinkedHashMap<String, String> responseMap = new LinkedHashMap<>();
            responseMap.put("token", paymentToken.getToken());
            responseMap.put("data", resBody);
            return responseMap;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage(), "PAYMENT_FAILED", NOT_ACCEPTABLE);
        }
    }

    private TokenResponse getPaymentTokenResponse(String authToken, MetaOrderEntity metaOrder,
                                                  OrderService.OrderValue orderValue, HttpClient client,
                                                  OrderResponse orderResponse, Long userId, PaymobSourceEntity source,
                                                  Long transactionId) throws BusinessException {
        PaymentRequest paymentRequest = PaymentRequest.fromOrderResponse(orderResponse, source);
        paymentRequest.setAuth_token(authToken);
        TokenResponse paymentToken = null;
        AddressesEntity shippingAddress;
        if (metaOrder.getSubMetaOrders() == null || metaOrder.getSubMetaOrders().isEmpty())
            shippingAddress = metaOrder.getSubOrders().stream().findAny().get().getAddressEntity();
        else
            shippingAddress = metaOrder.getSubMetaOrders().stream().findAny().get().getSubOrders().stream().findAny().get().getAddressEntity();

        JSONObject paymentJsonObject = new JSONObject();
        paymentJsonObject.put("auth_token", authToken);
        paymentJsonObject.put("amount_cents", orderResponse.getAmountCents());
        paymentJsonObject.put("expiration", 3600);
        paymentJsonObject.put("order_id", orderResponse.getId().toString());
        JSONObject billingDataJsonObject = new JSONObject();


        // TODO: add floor and shipping method
        billingDataJsonObject.put("email", metaOrder.getUser().getEmail());
        billingDataJsonObject.put("first_name", shippingAddress.getFirstName());
        billingDataJsonObject.put("last_name", shippingAddress.getLastName());
        billingDataJsonObject.put("phone_number", shippingAddress.getPhoneNumber());
        billingDataJsonObject.put("street", "NA");
        billingDataJsonObject.put("building", "NA");
        billingDataJsonObject.put("floor", "NA");
        billingDataJsonObject.put("apartment", "NA");
        billingDataJsonObject.put("shipping_method", "NA");
        billingDataJsonObject.put("postal_code", "NA");
        billingDataJsonObject.put("city", "NA");
        billingDataJsonObject.put("state", "NA");
        billingDataJsonObject.put("country", "NA");

        paymentJsonObject.put("billing_data", billingDataJsonObject);
        paymentJsonObject.put("currency", orderValue.currency);
        paymentJsonObject.put("integration_id", source.getValue());
        paymentJsonObject.put("lock_order_when_paid", "false");
        try {
            String paymentKeyUrl = payMobAccount.getApiUrl() + "/acceptance/payment_keys";
            HttpPost post = new HttpPost(paymentKeyUrl);

            Gson gson = getGson();

            post.setEntity(new StringEntity(paymentJsonObject.toString(), ContentType.APPLICATION_JSON));
            post.setHeader("Content-Type", APPLICATION_JSON_VALUE);
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                String resBody = readInputStream(response.getEntity().getContent());
                paymentToken = gson.fromJson(resBody, TokenResponse.class);

                createPaymentEntity(metaOrder.getId(), orderValue, paymentToken, userId, orderResponse.getId().toString(), transactionId);
            } else {
                throw new BusinessException(readInputStream(response.getEntity().getContent()), "PAYMENT_FAILED", NOT_ACCEPTABLE);
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage(), "PAYMENT_FAILED", NOT_ACCEPTABLE);
        }
        return paymentToken;
    }


    private PaymentEntity createPaymentEntity(long metaOrderId, OrderService.OrderValue orderValue, TokenResponse paymentToken, Long userId, String indicator, Long transactionId) {
        PaymentEntity payment = new PaymentEntity();
        payment.setStatus(PaymentStatus.STARTED);
        payment.setAmount(orderValue.amount);
        payment.setCurrency(orderValue.currency);
        payment.setUid(paymentToken.getToken());
        payment.setOperator(payMobAccount.getAccountId());
        payment.setExecuted(new Date());
        payment.setUserId(userId);
        payment.setMetaOrderId(metaOrderId);
        payment.setObject("{\"successIndicator\": \"" + indicator + "\",\"transactionId\": \"" + transactionId + "\"}");
        paymentsRepository.saveAndFlush(payment);
        return payment;
    }

    private OrderRequest fromOrderValue(OrderService.OrderValue metaOrder, String token, Long transactionId) {
        return OrderRequest.builder()
                .authToken(token)
                .merchant_order_id(transactionId)
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
            throw new BusinessException("Unable to identify payment gateway", "", INTERNAL_SERVER_ERROR);
        }
        classLogger.info("Setting up payment for meta order: {}", metaOrderId);
        return merchantAccount;
    }

    public void confirmCallbackSource(String hmac, WebhookCallbackResponse response,  String privateKey) {
        String message = getParamsConcatenatedString(response.getObj());

        String generatedPassword = new HmacUtils("HmacSHA512", privateKey).hmacHex(message);

        if (!Objects.equals(generatedPassword, hmac))
            throw new RuntimeBusinessException(UNAUTHORIZED, PAYMENT$CALLBACK$002);

        classLogger.info("Callback confirmed from Paymob");
    }

    private String getParamsConcatenatedString(RetrieveTransactionResponse response) {

        StringBuilder message = new StringBuilder();
        message.append(response.getAmount_cents());
        message.append(response.getCreated_at());
        message.append(response.getCurrency());
        message.append(response.isError_occured());
        message.append(response.isHas_parent_transaction());
        message.append(response.getId());
        message.append(response.getIntegration_id());
        message.append(response.is_3d_secure());
        message.append(response.is_auth());
        message.append(response.is_capture());
        message.append(response.is_refunded());
        message.append(response.is_standalone_payment());
        message.append(response.is_voided());
        message.append(response.getOrder().getId());
        message.append(response.getOwner());
        message.append(response.isPending());
        message.append(response.getSource_data().getPan());
        message.append(response.getSource_data().getSub_type());
        message.append(response.getSource_data().getType());
        message.append(response.isSuccess());

        return message.toString();
    }
    public void confirmPaymentThroughCallback(String hmac, WebhookCallbackResponse response, boolean yeshteryMetaOrder) throws BusinessException {
        classLogger.info("Callback called from Paymob, hmac: "+hmac);

        Map<String, Object> data = mapper.convertValue(response.getObj().getData(), Map.class);
        String transactionId = (String) data.get("transaction_no");
        PaymentEntity payment = paymentsRepository.findByObjectContaining(transactionId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, PAYMENT$CALLBACK$001, transactionId));

        String privateKey = getAccountForOrder(payment.getMetaOrderId()).getPrivateKey();

        confirmCallbackSource(hmac, response, privateKey);

        if (List.of(PAID, UNPAID, REFUNDED).contains(payment.getStatus()))
            return ;

        checkPaymentResponse(response.getObj(), payment);
        paymentCommons.finalizePaymentOnly(payment, yeshteryMetaOrder);
    }

    public void verifyAndStore(String orderUid, boolean yeshteryMetaOrder) throws BusinessException {
        PaymentEntity payment = paymentCommons.getPaymentForOrderUid(orderUid);

        if (payment == null) {
            classLogger.warn("No payment associated with order {}", orderUid);
            throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", NOT_ACCEPTABLE);
        }

        if (List.of(PAID, UNPAID, REFUNDED).contains(payment.getStatus())) {
            paymentCommons.finalizePayment(payment, yeshteryMetaOrder);
            return;
        }

        long orderId = payment.getMetaOrderId();
        payMobAccount = getAccountForOrder(orderId);
        if (payMobAccount == null) {
            throw new BusinessException("No account associated with UID: " + orderUid, "INVALID_INPUT", NOT_ACCEPTABLE);
        }

        if (payment.getStatus() != PaymentStatus.STARTED) {
            classLogger.error("Invalid state ({}) for payment {}", payment.getStatus(), payment.getId());
            throw new BusinessException("Invalid state for the payment ", "INVALID_INPUT", NOT_ACCEPTABLE);
        }
        TokenResponse authToken = getAuthToken();
        JSONObject json = new JSONObject(payment.getObject());
        if (!json.has("successIndicator")) {
            classLogger.error("Payment {} for order {} does not contain successIndicator!", payment.getId(), orderUid);
            throw new BusinessException("Payment for order does not contain successIndicator", "INTERNAL_ERROR", INTERNAL_SERVER_ERROR);
        }
        if (!json.has("transactionId")) {
            classLogger.error("Payment {} for order {} does not contain transactionId!", payment.getId(), orderUid);
            throw new BusinessException("Payment for order does not contain transactionId", "INTERNAL_ERROR", INTERNAL_SERVER_ERROR);
        }

//        String successIndicator = json.getString("successIndicator");
        Long transactionId = json.getLong("transactionId");

        String paymentDetailUrl = payMobAccount.getApiUrl() + "/ecommerce/orders/transaction_inquiry";

        String body = "{ \n" +
                "  \"auth_token\": \""+authToken.getToken()+"\",\n" +
                "  \"merchant_order_id\":  "+transactionId+"\n" +
                "}";

        try {
            HttpUriRequest request = RequestBuilder.post(paymentDetailUrl)
                    .setEntity(new StringEntity(body))
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Gson gson = getGson();
                String resBody = readInputStream(response.getEntity().getContent());
                RetrieveTransactionResponse status = gson.fromJson(resBody, RetrieveTransactionResponse.class);
                checkPaymentResponse(status, payment);

                paymentCommons.finalizePayment(payment, yeshteryMetaOrder);
            }
        } catch (IOException e) {
            throw new BusinessException("Couldn't connect to payment gateway: " + e.getMessage(), "PAYMENT_FAILED", NOT_ACCEPTABLE);
        }
    }

    private void checkPaymentResponse(RetrieveTransactionResponse response, PaymentEntity payment) {
        if (response != null) {
            if (!response.isSuccess()) {
                throw new RuntimeBusinessException("This transaction was declined by payment gateway or not paid yet", "PAYMENT_FAILED",
                        NOT_ACCEPTABLE);
            }
            if (response.is_refunded()) {
                payment.setStatus(REFUNDED);
            } else if (response.is_voided()) {
                payment.setStatus(UNPAID);
            } else {
                payment.setStatus(PAID);
            }
        }
    }

    private Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }
}
