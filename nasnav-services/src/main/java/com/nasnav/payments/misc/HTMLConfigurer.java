package com.nasnav.payments.misc;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public class HTMLConfigurer {

	// For testing purposes only!
	public static String getConfiguredHtml(String jsonResult, String template) {

		String htmlPage = null;
		InputStream is = HTMLConfigurer.class.getClassLoader().getResourceAsStream(template);
		if (is == null) {
			System.err.println("######## FILE NOT AVAILABLE");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		htmlPage = reader.lines().collect(Collectors.joining(System.lineSeparator()));

		String modified = htmlPage.replace("$rawJSON", jsonResult);
		try {
			JSONObject data = new JSONObject(jsonResult);
			for (String key: data.keySet()) {
				if (data.get(key)instanceof String) {
//					System.out.println(key + " #->  " + data.getString(key));
					modified = modified.replace("$" + key, data.getString(key));
				} else if (data.get(key)instanceof BigDecimal) {
						System.out.println(key + " !->  " + data.getBigDecimal(key).toString());
						modified = modified.replace("$" + key, data.getBigDecimal(key).toString());
				} else if (data.get(key)instanceof Double) {
					System.out.println(key + " !!->  " + data.getDouble(key));
					modified = modified.replace("$" + key, Double.toString(data.getDouble(key)));
				} else if (data.get(key)instanceof Integer) {
					System.out.println(key + " !->  " + data.getInt(key));
					modified = modified.replace("$" + key, Integer.toString(data.getInt(key)));
				} else {
					System.out.println(key + " ?->  " + data.get(key).getClass());
				}
			}
//			modified = modified
//					.replace("$order_id", data.getString("order_uid"))
//					.replace("$session_id", data.getString("session_id"))
//					.replace("$merchant", data.getString("merchant"))
//					.replace("$amount", data.getBigDecimal("order_amount").toString())
//					.replace("$currency", data.getString("order_currency"))
//					.replace("$confirm_url", data.getString("execute_url"))
//					.replace("$public_key", data.getString("public_key"))
//					.replace("$api_version", account.getApiVersion());
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
			ex.printStackTrace();
		}
		return modified;
	}
}
