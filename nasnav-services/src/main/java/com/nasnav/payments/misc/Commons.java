package com.nasnav.payments.misc;

import com.nasnav.AppConfig;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.Account;
import com.nasnav.payments.mastercard.MastercardAccount;
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
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

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
			}
		}
		return null;
	}

	public void finalizePayment (PaymentEntity payment)	throws BusinessException {

		ArrayList<OrdersEntity> orders = new ArrayList<>(ordersRepository.findByMetaOrderId(payment.getMetaOrderId()));

		paymentsRepository.saveAndFlush(payment);

		for (OrdersEntity order : orders) {
			order.setPaymentEntity(payment);
			orderService.setOrderAsPaid(payment, order);
			ordersRepository.saveAndFlush(order);
		}
		ordersRepository.flush();
		orderService.finalizeOrder(payment.getMetaOrderId());
	}

	public PaymentEntity getPaymentForOrderUid(String uid) {
		return paymentsRepository.findByUid(uid).orElse(null);
	}

	public OrderService.OrderValue getMetaOrderValue(long metaOrderId) {
		return orderService.getMetaOrderTotalValue(metaOrderId);
	}


    public String readInputStream(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }



}
