package com.nasnav.payments.misc;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.mastercard.MastercardSession;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.service.OrderService;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.toList;

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

	public static String getAccount(long metaOrderId, String gateway, OrderService orderService, OrganizationPaymentGatewaysRepository gatewaysRepo) throws BusinessException {
		ArrayList<OrdersEntity> subOrders = orderService.getOrdersForMetaOrder(metaOrderId);
		return getAccount(subOrders, gateway, gatewaysRepo);
	}

	public static String getAccount(ArrayList<OrdersEntity> orders, String gateway, OrganizationPaymentGatewaysRepository gatewaysRepo) throws BusinessException {
		long orgId = -1;
		for (OrdersEntity order: orders) {
			if (orgId != order.getOrganizationEntity().getId()) {
				if (orgId < 0) {
					orgId = order.getOrganizationEntity().getId();
				} else {
					throw new BusinessException("Orders belong to different organizations", "INVALID_ORDER", HttpStatus.NOT_ACCEPTABLE);
				}
			}
		}
		Optional<OrganizationPaymentGatewaysEntity> account = gatewaysRepo.findByOrganizationIdAndGateway(orgId, gateway);
		if (!account.isPresent()) {
			// use default account
			account = gatewaysRepo.getDefaultGateway(gateway);
		}

		return account.map(OrganizationPaymentGatewaysEntity::getAccount).orElse(null);
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

	public static String getOrderUid(long metaOrderId, Logger logger) throws BusinessException {
		if (metaOrderId < 1) {
			throw new BusinessException("Invalid order ID", "MISSING_ORDER", HttpStatus.NOT_ACCEPTABLE);
		}

		StringBuilder orderUid = new StringBuilder();
		orderUid.append(metaOrderId);
		orderUid.append("-");
		orderUid.append(new Date().getTime());
		return orderUid.toString();
	}


	public static Properties getPropertyForAccount(String accountName, Logger logger, String propertiesDir) {
		String file;
		if ("misr".equalsIgnoreCase(accountName)) {
			file = "/provider.banquemisr.properties";
		} else if ("qnb".equalsIgnoreCase(accountName)) {
			file = "/provider.qnb.properties";
		} else if ("rasports".equalsIgnoreCase(accountName)) {
			file = "/provider.rasports.properties";
		} else if ("rave".equalsIgnoreCase(accountName)) {
			file = "/provider.rave.properties";
		} else {
			logger.warn("Unknown account: {}", accountName);
			return null;
		}
		logger.debug("Attempting to load account properties file: {}", file);
		Properties props = null;
		try  {
			InputStream stream =
					Tools.class.getClass().getResourceAsStream(file);
			if (stream == null) {
				stream = new FileInputStream(new File(
					propertiesDir + file
				));
			}
			logger.info("Loaded account properties file: {}", file);
			props = new Properties();
			props.load(stream);
			stream.close();
		} catch (IOException e) {
			logger.error("Unable to load account property file: {}", file);
		}
		return props;
	}


	public static void validateOrdersForCheckOut(OrderService orderService, List<OrdersEntity> orders) {
		List<Long> orderIds =
				orders
						.stream()
						.map(OrdersEntity::getId)
						.collect(toList());
		orderService.validateOrderIdsForCheckOut(orderIds);
	}

}