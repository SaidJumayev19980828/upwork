package com.nasnav.payments.misr;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.payments.mastercard.Session;
import com.nasnav.payments.qnb.QnbAccount;
import com.nasnav.service.OrderService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MisrSession extends Session {

	@Setter
	QnbAccount qnbAccount;

	@Autowired
	public MisrSession(MisrAccount account, OrderService orderService, PaymentsRepository paymentsRepository, OrdersRepository ordersRepository) {
		super(account, orderService, paymentsRepository, ordersRepository);
	}

}
