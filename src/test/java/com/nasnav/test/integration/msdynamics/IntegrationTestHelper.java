package com.nasnav.test.integration.msdynamics;

import static com.nasnav.enumerations.PaymentStatus.PAID;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.test.commons.TestCommons.json;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;

@Component
public class IntegrationTestHelper {
	
	
	@Autowired
	private PaymentsRepository paymentRepo;
	
	
	@Autowired
	private OrdersRepository orderRepo;
	
	
	@Transactional
	public PaymentEntity updatePayment(Long id) {
		PaymentEntity payment = paymentRepo.getOne(id);
		payment.setAmount(new BigDecimal("888"));
		payment = paymentRepo.save(payment);
		
		return payment;
	}
	
	
	
	@Transactional
	public PaymentEntity createDummyPayment(Long orderId) {
				
		OrdersEntity order = orderRepo.findById(orderId).get();
		PaymentEntity payment = new PaymentEntity();
		JSONObject paymentObj = 
				json()
				.put("what_is_this?", "dummy_payment_obj");
		
		payment.setOperator("UPG");
		payment.setOrdersEntity(order);
		payment.setUid("MLB-<MerchantReference>");
		payment.setExecuted(new Date());
		payment.setObject(paymentObj.toString());
		payment.setAmount(new BigDecimal("600"));
		payment.setCurrency(EGP);
		payment.setStatus(PAID);
		
		payment= paymentRepo.save(payment);
		return payment;
	}
}
