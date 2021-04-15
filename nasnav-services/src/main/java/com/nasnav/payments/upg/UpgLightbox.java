package com.nasnav.payments.upg;

import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;


public class UpgLightbox {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	public ResponseEntity<?> callback(String content, UpgAccount account, OrderService orderService, Commons paymentCommons, MetaOrderRepository metaRepo, Logger upgLogger) throws BusinessException {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(content);
		} catch (JSONException ex) { ; }
		if (jsonObject == null) {
			upgLogger.error("Unable to parse the response: {}", content);
			return new ResponseEntity<>("{\"status\": \"ERROR\", \"message\": \"Unable to process the response received from the gateway\"}", BAD_GATEWAY);
		}
		// get the order id from merchant reference
		String ref = jsonObject.getString("MerchantReference");
		MetaOrderEntity metaOrderEntity = null;
		try {
			String metaOrderStr = ref.substring(0, ref.indexOf('-'));
			long metaOrderId = Long.parseLong(metaOrderStr);
			metaOrderEntity = metaRepo.findById(metaOrderId).get();
		} catch (Exception ex) {
			upgLogger.error("Unable to retrieve order from the reference: {}", ref);
			return new ResponseEntity<>("{\"status\": \"ERROR\", \"message\": \"Unable to process Order ID\"}", BAD_GATEWAY);
		}
		PaymentEntity payment = UpgLightbox.verifyPayment(jsonObject, metaOrderEntity, upgLogger, account, orderService);
		if (payment == null) {
			return new ResponseEntity<>("{\"status\": \"ERROR\", \"message\": \"Unable to verify payment confirmation\"}", BAD_REQUEST);
		}
		paymentCommons.finalizePayment(payment);

		return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", OK);
	}

	public JSONObject getJsonConfig(long metaOrderId, UpgAccount account, OrderService orderService, Logger upgLogger) throws BusinessException {
		Date now = new Date();
		String orderUid = Tools.getOrderUid(metaOrderId, upgLogger);

		OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderId);
		if (orderValue == null) {
			throw new BusinessException("Order ID is invalid", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}

		JSONObject result = new JSONObject();
		result.put("PaymentMethodFromLightBox", "2");
//		result.put("Currency", Integer.toString(818));     // only EGP support right now
		result.put("AmountTrxn", orderValue.amount.multiply(new BigDecimal(100)).intValue()); // amount in cents
//		result.put("OrderId", "");  // Gateway fails on sending orderId
		result.put("MID", account.getUpgMerchantId());
		result.put("TID", account.getUpgTerminalId());
		result.put("TrxDateTime", dateFormat.format(now));
		result.put("MerchantReference", orderUid);
//		result.put("ReturnUrl", "/");

		JSONObject chash = new JSONObject();
		chash.put("Amount", result.getBigDecimal("AmountTrxn")); // amount in cents
		chash.put("MerchantId", result.getString("MID"));
		chash.put("MerchantReference", result.getString("MerchantReference"));      // This is used by the new upgstaglightbox.egyptianbanks.com
		chash.put("TerminalId", result.getString("TID"));
		chash.put("DateTimeLocalTrxn", result.getString("TrxDateTime"));

		result.put("SecureHash", calculateHash(chash, account.getUpgSecureKey()));
		return result;
	}

	public static PaymentEntity verifyPayment(JSONObject json, MetaOrderEntity metaOrderEntity, Logger upgLogger, UpgAccount account, OrderService orderService) throws BusinessException {
//System.out.println("Received: " + json.toString(2));
		JSONObject verifier = new JSONObject();
		for (String param: new String[] {"Amount", "Currency", "MerchantReference", "PaidThrough", "TxnDate"}) {
			verifier.put(param, json.getString(param));
		}
		verifier.put("MerchantId", account.getUpgMerchantId());
		verifier.put("TerminalId", account.getUpgTerminalId());
		String hash = calculateHash(verifier, account.getUpgSecureKey());
		if (hash == null || !hash.toUpperCase().equals(json.getString("SecureHash").toUpperCase())) {
			upgLogger.error("Calculated hash {} does not match the received one {}", hash, json.get("SecureHash"));
			return null;
		}

		long paidAmount = 0;
		try {
			paidAmount = Long.parseLong(json.getString("Amount"));
		} catch (Exception ex) {;}

		OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderEntity.getId());
		if (orderValue == null) {
			upgLogger.error("Invalid order ID: {}", metaOrderEntity.getId());
			return null;
		}

		if (orderValue.amount.movePointRight(2).longValue() != paidAmount) {
			upgLogger.error("Paid amount: {} does not equal order {} amount: {}", json.getString("Amount"), metaOrderEntity.getId(), orderValue.amount.movePointRight(2));
			return null;
		}
		PaymentEntity payment = new PaymentEntity();
		payment.setOperator(account.getAccountId());
//      payment.setOrdersEntity(order);
		payment.setUid(json.getString("MerchantReference"));
		payment.setExecuted(new Date());
		payment.setStatus(PaymentStatus.PAID);
		payment.setAmount(new BigDecimal(paidAmount).movePointLeft(2));
		payment.setCurrency(TransactionCurrency.EGP);
		payment.setObject(json.toString());
		payment.setUserId(metaOrderEntity.getUser().getId());
		payment.setMetaOrderId(metaOrderEntity.getId());

		return payment;
	}

	public static String calculateHash(JSONObject data, String hashKey) {
		ArrayList<String> jsonKeys = new ArrayList<>();
		jsonKeys.addAll(data.keySet());
		Collections.sort(jsonKeys);
		StringBuilder args = new StringBuilder();
		for(String k: jsonKeys) {
			args.append(k);
			args.append('=');
			args.append(data.get(k));
			args.append('&');
		}
		String concat = args.toString().substring(0, args.length() - 1);
//		System.out.println("CONCAT: " + concat);
		try {
			byte[] keyBytes = Hex.decodeHex(hashKey);
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
			sha256_HMAC.init(secret_key);
			return Hex.encodeHexString(sha256_HMAC.doFinal(concat.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException | InvalidKeyException | DecoderException ex) {
			// TODO
			ex.printStackTrace();
		}
		return null;
	}

	public static String getConfiguredHtml(JSONObject data, String template, String callback) {
		String htmlPage = null;

		InputStream is = UpgLightbox.class.getClassLoader().getResourceAsStream(template);
		if (is == null) {
			System.err.println("######## LIGHTBOX TEMPLATE NOT AVAILABLE #######");
			return "";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		htmlPage = reader.lines().collect(Collectors.joining(System.lineSeparator()));
		StringBuilder args = new StringBuilder();
		for (String key: data.keySet()) {
			args.append("\t\t");
			args.append(key);
			if (data.get(key) instanceof Integer || data.get(key) instanceof Long) {
				args.append(": ");
				args.append(data.get(key));
				args.append(",\n");
			} else {
				args.append(": '");
				args.append((String) data.get(key));
				args.append("',\n");
			}
		}
		String modified = htmlPage;
		modified = modified.replace("$rawJSON", data.toString());
		modified = modified.replace("/*TRNDATA*/", args.toString());
		modified = modified.replace("/*CALLBACK*/", callback);

		return modified;
	}
}
