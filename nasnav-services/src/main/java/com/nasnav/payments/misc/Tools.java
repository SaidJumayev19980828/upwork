package com.nasnav.payments.misc;

import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.service.OrderService;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

public class Tools {


/*
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
*/

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

/*

	private static OrderService.OrderValue getTotalOrderValue(ArrayList<OrdersEntity> orders, OrderService orderService, Logger logger) throws BusinessException {
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
			oValue.currency = Commons.DEFAULT_CURRENCY_IF_NOT_SPECIFIED;
			logger.info("No currency specified for order, assuming {}", Commons.DEFAULT_CURRENCY_IF_NOT_SPECIFIED.name());
		}
		return oValue;
	}
*/

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

	public static long getOrderIdFromUid(String orderUid) throws BusinessException {
		if (orderUid != null || orderUid.indexOf('-') > 0) {
			try {
				return Long.parseLong(orderUid.substring(0, orderUid.indexOf('-')));
			} catch (Exception ex) {
				;
			}
		}
		throw new BusinessException("Invalid order UID", "INVALID_ORDER", HttpStatus.NOT_ACCEPTABLE);
	}


	public static synchronized Properties getPropertyForAccount(String accountName, Logger logger, String propertiesDir) {

		Properties props = null;
		String file = null;

		try  {
			if (propertiesDir == null || propertiesDir.equals("#")) {
				// load file from bundled resources
				file = "/" + accountName;
			} else {
				file = propertiesDir + accountName;
			}
			logger.debug("Attempting to load account properties from resource: {}", file);
			InputStream stream = Tools.class.getResourceAsStream(file);
			if (stream == null) {
				stream = new FileInputStream(file);
			}
			logger.info("Loading account properties from resource: {}", file);
			props = new Properties();
			props.load(stream);
			stream.close();
		} catch (IOException e) {
			logger.error("Unable to load account property: {}, {}", file, e.getMessage());
		}
		if (props == null) {
			logger.error("Properties ({}) not loaded", accountName);
		} else {
			logger.debug("Properties ID: {}", props.getProperty("account.identifier"));
		}
		return props;
	}

}
