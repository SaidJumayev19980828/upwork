package com.nasnav.payments.qnb;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.payments.mastercard.Session;
import com.nasnav.service.OrderService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QnbSession extends Session {

	@Setter
	QnbAccount qnbAccount;

	@Autowired
	public QnbSession(QnbAccount account, OrderService orderService, PaymentsRepository paymentsRepository, OrdersRepository ordersRepository) {
		super(account, orderService, paymentsRepository, ordersRepository);
	}

}
