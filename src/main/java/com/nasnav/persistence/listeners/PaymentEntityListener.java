package com.nasnav.persistence.listeners;

import java.util.Objects;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.integration.IntegrationServiceHelper;
import com.nasnav.persistence.PaymentEntity;




@Component
public class PaymentEntityListener {
	
	private static IntegrationServiceHelper integrationHelper;
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceHelper integrationHelper) {
		PaymentEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostPersist	
	@PostUpdate
	public void postPresist(PaymentEntity payment) {	
		if( Objects.equals(payment.getStatus(), PaymentStatus.PAID) ) {
			integrationHelper.pushNewPaymentEvent(payment);
		}		
	}




	
}
