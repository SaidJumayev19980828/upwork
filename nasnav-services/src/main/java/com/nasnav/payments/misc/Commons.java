package com.nasnav.payments.misc;

import com.nasnav.AppConfig;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.Account;
import com.nasnav.payments.mastercard.MastercardAccount;
import com.nasnav.payments.paymob.PayMobAccount;
import com.nasnav.payments.rave.RaveAccount;
import com.nasnav.payments.upg.UpgAccount;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.O$GNRL$0002;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class Commons {

	private static final Logger classLogger = LogManager.getLogger("Payment:COMMONS");

	@Autowired
	private AppConfig config;

	@Autowired
	private MetaOrderRepository metaOrderRepo;

	@Autowired
	private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;

	@Autowired
	private PaymentsRepository paymentsRepository;

	@Autowired
	OrdersRepository ordersRepository;

	@Autowired
	OrderService orderService;

	public OrganizationPaymentGatewaysEntity getPaymentAccount(Long metaOrderId, Gateway gateway) {

		MetaOrderEntity metaOrder = metaOrderRepo.findById(metaOrderId).orElse(null);
		if (metaOrder == null) {
			classLogger.warn("Unable to find meta order: {}", metaOrderId);
			return null;
		}
		long orgId = metaOrder.getOrganization().getId();

		Optional<OrganizationPaymentGatewaysEntity> opg = orgPaymentGatewaysRep.findByOrganizationIdAndGateway(orgId, gateway.getValue());
		return opg.orElse(orgPaymentGatewaysRep.getDefaultGateway(gateway.getValue()).orElse(null));
	}

	public Properties getAccountProperties(OrganizationPaymentGatewaysEntity gatewayEntity) {

		if (gatewayEntity == null) {
			classLogger.error("Missing gateway entity");
			return null;
		}
		Properties props = Tools.getPropertyForAccount(gatewayEntity.getAccount(), classLogger, config.paymentPropertiesDir);
		if (props == null) {
			classLogger.error("Unable to load properties for account {} of org_payment {}", gatewayEntity.getAccount(), gatewayEntity.getId());
			return null;
		}
		return props;
	}

	public Account getMerchantAccount(Long metaOrderId, Gateway gateway) {
		OrganizationPaymentGatewaysEntity gatewayEntity = getPaymentAccount(metaOrderId, gateway);
		Properties props = getAccountProperties(gatewayEntity);
		classLogger.debug("Getting merchant account for MetaOrder {}, gateway: {}, props: {}, entity: {}", metaOrderId, gateway.name(), props, gatewayEntity);
		if (props != null) {
			switch (gateway) {
				case MASTERCARD:
					MastercardAccount acc = new MastercardAccount();
					acc.init(props, gatewayEntity.getId());
					return acc;
				case UPG:
					UpgAccount acc2 = new UpgAccount();
					acc2.init(props);
					return acc2;
				case RAVE:
					return new RaveAccount(props, gatewayEntity.getId());
				case PAY_MOB:
					return new PayMobAccount(props, gatewayEntity.getId());
			}
		}
		return null;
	}

	public void finalizePaymentOnly(PaymentEntity payment, boolean yeshteryMetaOrder) {
		MetaOrderEntity metaOrder = metaOrderRepo.findByMetaOrderId(payment.getMetaOrderId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0002, payment.getMetaOrderId()));
		Set<OrdersEntity> orders = getSubOrders(payment, metaOrder, yeshteryMetaOrder);

		paymentsRepository.saveAndFlush(payment);

		linkPaymentToOrders(payment, orders);
	}

	public void finalizePayment(PaymentEntity payment, boolean yeshteryMetaOrder) {
		MetaOrderEntity metaOrder;
		if (yeshteryMetaOrder) {
			metaOrder = metaOrderRepo.findYeshteryMetaorderByMetaOrderId(payment.getMetaOrderId())
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0002, payment.getMetaOrderId()));
		} else {
			metaOrder = metaOrderRepo.findByMetaOrderId(payment.getMetaOrderId())
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0002, payment.getMetaOrderId()));
		}
		Set<OrdersEntity> orders = getSubOrders(payment, metaOrder, yeshteryMetaOrder);

		paymentsRepository.saveAndFlush(payment);

		linkPaymentToOrders(payment, orders);

		ordersRepository.flush();
		if (yeshteryMetaOrder) {
			orderService.finalizeYeshteryMetaOrder(metaOrder, orders);
		} else {
			orderService.finalizeOrder(metaOrder.getId());
		}
	}

	private void linkPaymentToOrders(PaymentEntity payment, Set<OrdersEntity> orders) {
		for (OrdersEntity order : orders) {
			order.setPaymentEntity(payment);
			orderService.setOrderAsPaid(payment, order);
			ordersRepository.saveAndFlush(order);
		}
	}

	private Set<OrdersEntity> getSubOrders(PaymentEntity payment, MetaOrderEntity metaOrder, boolean yeshteryMetaOrder) {
		Set<OrdersEntity> orders = metaOrder.getSubOrders();
		if (yeshteryMetaOrder) {
			orders.addAll(metaOrderRepo
					.findYeshteryMetaorderByMetaOrderId(payment.getMetaOrderId())
					.get()
					.getSubMetaOrders()
					.stream()
					.map(MetaOrderEntity::getSubOrders)
					.flatMap(Set::stream)
					.collect(toSet())
			);
		}
		return orders;
	}

	public PaymentEntity getPaymentForOrderUid(String uid) {
		return paymentsRepository.findByUid(uid).orElse(null);
	}

	public OrderService.OrderValue getMetaOrderValue(long metaOrderId) {
		return orderService.getMetaOrderTotalValue(metaOrderId);
	}

	public PaymentEntity getPaymentForMetaOrderId(long metaOrderId) {
		return paymentsRepository.findByMetaOrderId(metaOrderId).orElse(null);
	}


	public String readInputStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }



}
