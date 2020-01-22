package com.nasnav.persistence.listeners;

import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;

import java.util.Objects;

import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nasnav.integration.IntegrationServiceHelper;
import com.nasnav.persistence.OrdersEntity;




@Component
public class OrdersEntityListener {
	
	private static IntegrationServiceHelper integrationHelper;
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceHelper integrationHelper) {
		OrdersEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostUpdate
	public void postUpdate(OrdersEntity order) {	
		if( Objects.equals(order.getStatus(), CLIENT_CONFIRMED.getValue()) ) {
			integrationHelper.pushOrderConfirmEvent(order);
		}		
	}




	
}
