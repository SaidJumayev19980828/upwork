package com.nasnav.persistence.listeners;

import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;

import java.util.Objects;

import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nasnav.integration.IntegrationServiceHelper;
import com.nasnav.persistence.OrdersEntity;
import com.sun.istack.logging.Logger;




@Component
public class OrdersEntityListener {
	
	private static IntegrationServiceHelper integrationHelper;
	private static Logger logger = Logger.getLogger(OrdersEntityListener.class);
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceHelper integrationHelper) {
		OrdersEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostUpdate
	public void postPresist(OrdersEntity order) {	
		if( Objects.equals(order.getStatus(), CLIENT_CONFIRMED.getValue()) ) {
			integrationHelper.pushOrderConfirmEvent(order);
		}		
	}




	
}
