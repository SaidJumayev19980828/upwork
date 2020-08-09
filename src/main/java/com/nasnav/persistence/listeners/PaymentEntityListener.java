package com.nasnav.persistence.listeners;

import java.util.Objects;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.integration.IntegrationServiceAdapter;
import com.nasnav.persistence.PaymentEntity;




@Component
public class PaymentEntityListener {
	
	private static IntegrationServiceAdapter integrationHelper;
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceAdapter integrationHelper) {
		PaymentEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostPersist	
	public void postPresist(PaymentEntity payment) {	
		postPresistLogic(payment);
	}

	
	
	
	
	@PostUpdate
	public void postUpdate(PaymentEntity payment) {
		postPresistLogic(payment);
	}
	
	
	
	
	public void postPresistLogic(PaymentEntity payment) {	
		if( Objects.equals(payment.getStatus(), PaymentStatus.PAID) ) {
			integrationHelper.pushPaymentEvent(payment);
		}		
	}

	
}
