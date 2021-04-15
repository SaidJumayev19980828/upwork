package com.nasnav.controller;

import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.cod.CodCommons;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Gateway;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import com.nasnav.shipping.services.PickupFromShop;
import com.nasnav.shipping.services.PickupPointsWithInternalLogistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static com.nasnav.payments.cod.CodCommons.COD_OPERATOR;
import static com.nasnav.payments.cod.CodCommons.isCodAvailableForService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/cod")
public class PaymentControllerCoD {



	private static final Logger codLogger = LogManager.getLogger("Payment:COD");

	@Autowired
	private Commons paymentCommons;

	@Autowired
	private OrdersRepository ordersRepository;

	@Autowired
	private MetaOrderRepository metaOrdersRepository;

	@Autowired
	private OrderService orderService;

	@Autowired
	public PaymentControllerCoD() {

	}



	@RequestMapping(value = "execute")
	public ResponseEntity<?> payCoD(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
		ArrayList<OrdersEntity> orders = orderService.getOrdersForMetaOrder(metaOrderId);

		Optional<MetaOrderEntity> metaOrderOpt = metaOrdersRepository.findById(metaOrderId);
		if (!metaOrderOpt.isPresent()) {
			throw new BusinessException("Order ID is invalid", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}

		OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderId);
		if (orderValue == null) {
			throw new BusinessException("Order ID is invalid", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}
		// check if CoD is avaialble
		if (paymentCommons.getPaymentAccount(metaOrderId, Gateway.COD) == null) {
			throw new BusinessException("CoD payment not available for order", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}
		for (OrdersEntity subOrder: ordersRepository.findByMetaOrderId(metaOrderId)) {
			if (subOrder.getShipment() != null && !isCodAvailableForService(subOrder.getShipment().getShippingServiceId())) {
				codLogger.warn("Sub-order ({}) marked for pickup, COD not allowed.", subOrder.getId());
				throw new BusinessException("At least one of the sub-orders marked for pickup", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
			}
		}

		PaymentEntity payment = new PaymentEntity();
		payment.setOperator(COD_OPERATOR);
		payment.setUid(Tools.getOrderUid(metaOrderId,codLogger));
		payment.setExecuted(new Date());
		payment.setStatus(PaymentStatus.COD_REQUESTED);
		payment.setAmount(orderValue.amount);
		payment.setCurrency(orderValue.currency);
		// TODO: this shall probably be changed to logged-in user rather than the meta-order owner later on
		payment.setUserId(metaOrderOpt.get().getUser().getId());
		payment.setMetaOrderId(metaOrderId);

		paymentCommons.finalizePayment(payment);

		return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);

	}
}