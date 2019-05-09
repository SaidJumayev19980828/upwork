package com.nasnav.payments.qnb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.nasnav.exceptions.BusinessException;

@Service
public class PaymentService {

	@Autowired
	private BasketRepository basketRepository;

	@Autowired
	private OrderRepository orderRepository;

	// For testing purposes only!
	public String getConfiguredHtml(String jsonResult) throws BusinessException {

		StringBuilder htmlPage = null;
		try {
			File file = ResourceUtils.getFile("classpath:static/session.html");
			htmlPage = readInputStream(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String modified = htmlPage.toString().replace("$rawJSON", jsonResult);
		try {
			JSONObject data = new JSONObject(jsonResult);
			modified = modified
					.replace("$order_id", data.getString("order_uid"))
					.replace("$session_id", data.getString("session_id"))
//					.replace("$merchant", data.getString("merchant"))
					.replace("$amount", data.getBigDecimal("order_amount").toString())
					.replace("$currency", data.getString("order_currency"))
					.replace("$confirm_url", data.getString("execute_url"));
//			if (data.getJSONObject("session") != null) {
//				modified = modified.replace("$session_id", data.getJSONObject("session").getString("id"));
//			}
/*
			if (data.getJSONObject("seller") != null) {
				modified = modified.replace("$seller_name", data.getJSONObject("seller").getString("organization_name"));
				modified = modified.replace("$seller_address_1", data.getJSONObject("seller").getString("address_line1"));
				modified = modified.replace("$seller_address_2", data.getJSONObject("seller").getString("address_line2"));
				modified = modified.replace("$seller_logo", data.getJSONObject("seller").getString("logo_url"));
			}
*/
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return modified;
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
}