package com.nasnav.payments.qnb;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.nasnav.exceptions.BusinessException;

@Service
public class PaymentService {

	// For testing purposes only!
	public String getConfiguredHtml(String jsonResult, String template) throws BusinessException {

		String htmlPage = null;
//		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream(template);
			if (is == null) {
				System.err.println("######## FILE NOT AVAILABLE");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			htmlPage = reader.lines().collect(Collectors.joining(System.lineSeparator()));

		String modified = htmlPage.replace("$rawJSON", jsonResult);
		try {
			Account merchant = new Account();
			JSONObject data = new JSONObject(jsonResult);
			modified = modified
					.replace("$order_id", data.getString("order_uid"))
					.replace("$session_id", data.getString("session_id"))
					.replace("$merchant", data.getString("merchant"))
					.replace("$amount", data.getBigDecimal("order_amount").toString())
					.replace("$currency", data.getString("order_currency"))
					.replace("$confirm_url", data.getString("execute_url"))
					.replace("$api_version", merchant.getApiVersion());
//			if (data.getJSONObject("session") != null) {
//				modified = modified.replace("$session_id", data.getJSONObject("session").getString("id"));
//			}
			if (data.has("seller")) {
				modified = modified.replace("$seller_name", data.getJSONObject("seller").getString("organization_name"));
				modified = modified.replace("$seller_address_1", data.getJSONObject("seller").getString("address_line1"));
				modified = modified.replace("$seller_address_2", data.getJSONObject("seller").getString("address_line2"));
				modified = modified.replace("$seller_logo", data.getJSONObject("seller").getString("logo_url"));
			} else {
				modified = modified.replace("$seller_name", "Seller's Name");
				modified = modified.replace("$seller_address_1", "Address 1");
				modified = modified.replace("$seller_address_2", "Address 2");
				modified = modified.replace("$seller_logo",  "http://nasnav.com/logo.png");
			}

		} catch (Exception ex) {
			System.out.println(ex);
		}
		return modified;
	}

/*
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
*/
}