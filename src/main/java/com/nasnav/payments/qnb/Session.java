package com.nasnav.payments.qnb;

import com.nasnav.dao.BasketRepository;
import com.nasnav.dto.OrderSessionBasket;
import com.nasnav.persistence.BasketsEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Session {

    @Autowired
    private BasketRepository basketRepository;

    public static enum TransactionCurrency { 
    	EGP(0),USD(1);
    	int value;
    	private TransactionCurrency(int value) {
			this.value = value;
		}
    	public static TransactionCurrency getTransactionCurrency(int value) {
    		
    		for(TransactionCurrency transactionCurrency : TransactionCurrency.values()) {
    			if(transactionCurrency.value==value)
    				return transactionCurrency;
    		}
    		return null;
    	}
    };

    Account merchantAccount;
    String result;
    String indicator;
    String updateStatus;
    String sessionId;
    String orderUid;
    String version;

    private static final Logger qnbLogger = LogManager.getLogger("Payment:QNB");

    public Session(Account account) {
        this.merchantAccount = account;
    }

    private String getCurrencyStr(TransactionCurrency currency) {
        return currency.name();
    }

    private StringBuilder readInputStream(InputStream stream) throws IOException {
        int charsRead;
        byte[] byteArray = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while ((charsRead = stream.read(byteArray)) > 0) {
            String line = new String(byteArray, StandardCharsets.UTF_8);
            sb.append(line, 0, charsRead);
        }
        return sb;
    }

    public boolean initialize(long orderId, TransactionCurrency currency) {

        String authString = merchantAccount.getApiUsername()
                + ":" + merchantAccount.getApiPassword();
        String authStringEnc = new String(
                Base64.encodeBase64(authString.getBytes()));

        JSONObject order = new JSONObject();
        orderUid = Long.toString(orderId) + "#" + Long.toString(new Date().getTime());
        order.put("id", orderUid);
        order.put("currency", getCurrencyStr(currency));
        order.put("basket", getBasketFromOrderId(orderId));
        order.put("order_value", getBasketsTotalAmount(orderId));

        JSONObject data = new JSONObject();
        data.put("apiOperation", "CREATE_CHECKOUT_SESSION");
        data.put("order", order);



        try {
            HttpClient client= HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(merchantAccount.getApiUrl()
                    + "/merchant/" + merchantAccount.getMerchantId() +"/session");

            StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            request.setHeader("Authorization", "Basic " + authStringEnc);

            HttpResponse response = client.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status > 299) {
                StringBuilder errorResponse = readInputStream(response.getEntity().getContent());
                qnbLogger.error("Attempt to set up hosted session resulted in {}. Error provided: {}", status, errorResponse);
                return false;
            }
            JSONObject jsonResult = new JSONObject(readInputStream(response.getEntity().getContent()).toString());
            result = jsonResult.getString("result");
            indicator = jsonResult.getString("successIndicator");
            JSONObject jsonSession = jsonResult.getJSONObject("session");
            updateStatus = jsonSession.getString("updateStatus");
            sessionId = jsonSession.getString("id");
            version = jsonSession.getString("version");

            if ("SUCCESS".equalsIgnoreCase(result) && "SUCCESS".equalsIgnoreCase(updateStatus)) {
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

    public String getMerchantId() {
        return this.merchantAccount == null ? null : this.merchantAccount.getMerchantId();
    }

    public List<OrderSessionBasket> getBasketFromOrderId(Long order_id) {
        List<OrderSessionBasket> baskets = new ArrayList<>();
        List<BasketsEntity> basketsEntity = basketRepository.findByOrdersEntity_Id(order_id);
        for (BasketsEntity basketEntity : basketsEntity){
            OrderSessionBasket basket = new OrderSessionBasket();
            basket.setName(basketEntity.getStocksEntity().getProductEntity().getName());
            basket.setPrice(basketEntity.getPrice());
            basket.setQuantity(basketEntity.getQuantity());
            baskets.add(basket);
        }
        return baskets;
    }

    public BigDecimal getBasketsTotalAmount(Long order_id){
        List<BasketsEntity> basketsEntity = basketRepository.findByOrdersEntity_Id(order_id);
        return basketsEntity.stream().map(BasketsEntity::getPrice).reduce(BigDecimal::add).get();
    }
}
