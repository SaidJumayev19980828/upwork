package com.nasnav.controller;

import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



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
		PaymentEntity payment = orderService.validateOrderForPaymentCoD(metaOrderId);

		paymentCommons.finalizePayment(payment, false);

		return new ResponseEntity<>("{\"status\": \"SUCCESS\"}", HttpStatus.OK);

	}
}