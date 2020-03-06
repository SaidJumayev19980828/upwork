package com.nasnav.payments.misc;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.mastercard.MastercardSession;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

public class Tools {

	public static ArrayList<OrdersEntity> getOrdersFromString(OrdersRepository ordersRepository, String ordersList, String separator) throws BusinessException {
		if (ordersList == null || ordersList.length() == 0) {
			throw new BusinessException("Empty orders list", "INVALID_ORDER", HttpStatus.NOT_ACCEPTABLE);
		}
		try {
			ArrayList<OrdersEntity> result = new ArrayList<>();
			for (String orderStr : ordersList.split(separator)) {
				long orderId = Long.parseLong(orderStr);
				Optional<OrdersEntity> oo = ordersRepository.findById(orderId);
				if (!oo.isPresent()) {
					throw new BusinessException("Order " + orderId + " does not exist", "INVALID_ORDER", HttpStatus.NOT_ACCEPTABLE);
				}
				result.add(oo.get());
			}
			return result;
		} catch (NumberFormatException ex) {
			throw new BusinessException("Unable to parse orders list", "INVALID_ORDER", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	public static OrderService.OrderValue getTotalOrderValue(ArrayList<OrdersEntity> orders, OrderService orderService, Logger logger) throws BusinessException {
		if (orders == null || orders.size() < 1) {
			throw new BusinessException("Empty order list", "MISSING_ORDER", HttpStatus.NOT_ACCEPTABLE);
		}

		OrderService.OrderValue oValue = new OrderService.OrderValue();
		oValue.currency = null;
		oValue.amount = new BigDecimal(0);
		for (OrdersEntity oe : orders) {
			OrderService.OrderValue ov = orderService.getOrderValue(oe);
			if (oValue.currency == null) {
				oValue.currency = ov.currency;
			} else {
				if (oValue.currency != ov.currency) {
					logger.warn("Cannot process order {} which contains items in different currencies {}:{}", oe.getId(), ov.currency, oValue.currency);
					throw new BusinessException("Cannot process order which contains items in different currencies", "ERROR_MIXED_CURRENCIES", HttpStatus.NOT_ACCEPTABLE);
				}
			}
			oValue.amount = oValue.amount.add(ov.amount);
		}
		if (oValue.currency == null) {
			oValue.currency = MastercardSession.DEFAULT_CURRENCY_IF_NOT_SPECIFIED;
			logger.info("No currency specified for order, assuming {}", MastercardSession.DEFAULT_CURRENCY_IF_NOT_SPECIFIED.name());
		}
		return oValue;
	}

	public static String getOrderUid(ArrayList<OrdersEntity> orders, Logger logger) throws BusinessException {
		if (orders == null || orders.size() < 1) {
			throw new BusinessException("Empty order list", "MISSING_ORDER", HttpStatus.NOT_ACCEPTABLE);
		}
		long userId = orders.get(0).getUserId();

		StringBuilder orderUid = new StringBuilder();
		for (OrdersEntity oe : orders) {
			if (oe.getUserId() != userId) {
				logger.warn("Cannot process order {} which belong to multiple different users.", orderUid);
				throw new BusinessException("Cannot process orders belonging to diffrent users", "ERROR_MULTIPLE_USERS", HttpStatus.NOT_ACCEPTABLE);
			}
			if (orderUid.length() > 0) {
				orderUid.append(".");
			}
			orderUid.append(oe.getId());

		}
		// Calculate the total value of the orders, validate currencies
		orderUid.append("-");
		orderUid.append(new Date().getTime());
		return orderUid.toString();
	}


	public static Properties getPropertyForAccount(String accountName, Logger logger) {
		String file = null;
		if ("misr".equalsIgnoreCase(accountName)) {
			file = "/provider.banquemisr.properties";
		} else if ("qnb".equalsIgnoreCase(accountName)) {
			file = "/provider.qnb.properties";
		} else if ("rave".equalsIgnoreCase(accountName)) {
			file = "/provider.rave.properties";
		} else {
			logger.warn("Unknown account: {}", accountName);
			return null;
		}
		try (final InputStream stream =
				     Tools.class.getClass().getResourceAsStream(file)) {
System.out.println(" USER DIR: " + System.getProperty("user.dir"));
System.out.println(" FILE: " + file);
System.out.println(" STRAM: " + stream);
			Properties props = new Properties();
			props.load(stream);
			return props;
		} catch (IOException e) {
			logger.error("Unable to load account property file: {}", file);
			return null;
		}
	}
}