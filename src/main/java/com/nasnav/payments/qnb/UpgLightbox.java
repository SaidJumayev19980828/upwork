package com.nasnav.payments.qnb;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class UpgLightbox {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static QnbAccount account = new QnbAccount();
	private static final Logger qnbLogger = LogManager.getLogger("Payment:QNB");

	public JSONObject getJsonConfig(OrdersEntity order) {
		Date now = new Date();
		JSONObject result = new JSONObject();
		result.put("PaymentMethodFromLightBox", 0);
//		result.put("Currency", Integer.toString(818));     // only EGP support right now
		result.put("AmountTrxn", order.getAmount().multiply(new BigDecimal(100)).intValue()); // amount in cents
//		result.put("OrderId", Long.toString(order.getId()));  // Gateway fails on sending orderId
		result.put("MID", account.getUpgMerchantId());
		result.put("TID", account.getUpgTerminalId());
		result.put("TrxDateTime", dateFormat.format(now));
		result.put("MerchantReference", order.getId() + "-" + now.getTime());
//		result.put("ReturnUrl", "/");

		JSONObject chash = new JSONObject();
		chash.put("Amount", order.getAmount().multiply(new BigDecimal(100)).intValue()); // amount in cents
		chash.put("MerchantId", account.getUpgMerchantId());
		chash.put("TerminalId", account.getUpgTerminalId());
		chash.put("DateTimeLocalTrxn", dateFormat.format(now));

		result.put("SecureHash", calculateHash(chash, account.getUpgSecureKey()));
		return result;
	}

	public static PaymentEntity verifyPayment(JSONObject json, OrdersEntity order) {
//System.out.println("Received: " + json.toString(2));
		JSONObject verifier = new JSONObject();
		for (String param: new String[] {"TxnDate", "Amount", "Currency", "PaidThrough"}) {
			verifier.put(param, json.getString(param));
		}
		verifier.put("MerchantId", account.getUpgMerchantId());
		verifier.put("TerminalId", account.getUpgTerminalId());
		String hash = calculateHash(verifier, account.getUpgSecureKey());
		if (hash == null || !hash.toUpperCase().equals(json.getString("SecureHash").toUpperCase())) {
			qnbLogger.error("Calculated hash {} does not match the received one {}", hash, json.get("SecureHash"));
			return null;
		}
//System.out.println("received hash: " + json.get("SecureHash") + "\ncalculated: " + hash);

		long paidAmount = 0;
		try {
			paidAmount = Long.parseLong(json.getString("Amount"));
		} catch (Exception ex) {;}
		if (order.getAmount().longValue() * 100 != paidAmount) {
			qnbLogger.error("Paid amount: {} does not equal order {} amount: {}", json.getString("Amount"), order.getId(), order.getAmount());
			return null;
		}
		PaymentEntity payment = new PaymentEntity();
		payment.setOperator("UPG");
		payment.setOrdersEntity(order);
		payment.setUid("MLB-" + json.getString("MerchantReference"));
		payment.setExecuted(new Date());
		payment.setStatus(PaymentStatus.PAID);
		payment.setAmount(new BigDecimal(paidAmount).movePointLeft(2));
		payment.setCurrency(TransactionCurrency.EGP);
		payment.setObject(json.toString());
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
//System.out.println("CONCAT: " + concat);
		try {
			byte[] keyBytes = Hex.decodeHex(hashKey);
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
			sha256_HMAC.init(secret_key);
			return Hex.encodeHexString(sha256_HMAC.doFinal(concat.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | DecoderException ex) {
			// TODO
			ex.printStackTrace();
		}
		return null;
	}

	public String getConfiguredHtml(JSONObject data, String template) {
		String htmlPage = null;
//		try {
		InputStream is = getClass().getClassLoader().getResourceAsStream(template);
		if (is == null) {
			System.err.println("######## LIGHTBOX TEMPLATE NOT AVAILABLE #######");
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
		modified = modified.replace("/*CALLBACK*/", account.getUpgCallbackUrl());
/*
		try {
		} catch (Exception ex) {
			System.out.println(ex);
		}
*/
		return modified;

	}
}
