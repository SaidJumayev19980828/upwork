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
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
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

	public String getConfiguredHtml(Long orderId, Session session) throws BusinessException {

		Optional<OrdersEntity> ordersEntityOptional = orderRepository.findById(orderId);

		if(ordersEntityOptional==null || !ordersEntityOptional.isPresent()) {
			throw new BusinessException("Order not found", null, HttpStatus.NOT_FOUND);
		}
		List<BasketsEntity> orderBaskets = basketRepository.findByOrdersEntity_Id(orderId);

		if(orderBaskets==null || orderBaskets.isEmpty()) {
			throw new BusinessException("Order not found", null, HttpStatus.NOT_FOUND);
		}

		BigDecimal total = orderBaskets.stream().map(BasketsEntity::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
		String email = ordersEntityOptional.get().getEmail();
		int currencyId = orderBaskets.get(0).getCurrency();

		/*
		String email = "ahmed.saeed@nasnav.com";
		int currencyId = 0;
		Double total = 1000d; */

		StringBuilder htmlPage = null;
		File file = null;
		try {
			file = ResourceUtils.getFile("classpath:static/checkout.html");

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new BusinessException(e1.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {
			htmlPage = readInputStream(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		String modified = htmlPage.toString();
		modified = modified.replace("$order_id", session.getOrderRef())
				.replace("$merchant", session.getMerchantId())
				.replace("$amount", total + "")
				.replace("$currency", Session.TransactionCurrency.getTransactionCurrency(currencyId).name())
				.replace("$email", email)
				.replace("$session_id", session.getSessionId())
				.replace("$order_value", total+"")
				.replace("$basket", session.getBasketFromOrderId(orderId));

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