package com.nasnav.persistence.listeners;

import static com.nasnav.enumerations.OrderStatus.FINALIZED;

import java.util.Objects;

import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nasnav.integration.IntegrationServiceAdapter;
import com.nasnav.persistence.OrdersEntity;




@Component
public class OrdersEntityListener {
	
	private static IntegrationServiceAdapter integrationHelper;
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceAdapter integrationHelper) {
		OrdersEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostUpdate
	public void postUpdate(OrdersEntity order) {	
		//make sure the event push logic is called after the
		//transaction is complete
		TransactionSynchronizationManager
		.registerSynchronization( 
	            new TransactionSynchronizationAdapter() {
	                @Override
	                public void afterCommit() {
	                	if( Objects.equals(order.getStatus(), FINALIZED.getValue()) ) {
	            			integrationHelper.pushOrderConfirmEvent(order);
	            		}	
	                }
	            });
	}




	
}
