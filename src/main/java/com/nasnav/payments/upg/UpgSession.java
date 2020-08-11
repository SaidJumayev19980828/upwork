package com.nasnav.payments.upg;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.payments.mastercard.MastercardAccount;
import com.nasnav.service.OrderService;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpgSession {

	@Getter
	UpgAccount upgAccount;

	@Getter
	OrderService orderService;

//	private final PaymentsRepository paymentsRepository;
//
//	private final OrdersRepository ordersRepository;

	@Autowired
	public UpgSession(UpgAccount account, OrderService orderService, PaymentsRepository paymentsRepository, OrdersRepository ordersRepository) {
		this.orderService = orderService;
		this.upgAccount = account;
//		this.paymentsRepository = paymentsRepository;
//		this.ordersRepository = ordersRepository;
	}


}
