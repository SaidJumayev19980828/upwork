package com.nasnav.persistence.listeners;

import com.nasnav.integration.IntegrationServiceAdapter;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.PostUpdate;
import java.util.Objects;

import static com.nasnav.enumerations.OrderStatus.FINALIZED;


@Component
public class MetaOrderEntityListener {
	
	private static IntegrationServiceAdapter integrationHelper;
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceAdapter integrationHelper) {
		MetaOrderEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostUpdate
	public void postUpdate(MetaOrderEntity order) {
		//make sure the event push logic is called after the
		//transaction is complete
		TransactionSynchronizationManager
		.registerSynchronization( 
	            new TransactionSynchronizationAdapter() {
	                @Override
	                public void afterCommit() {
	                	if( Objects.equals(order.getStatus(), FINALIZED.getValue()) ) {
	            			integrationHelper.pushMetaOrderFinalizeEvent(order);
	            		}	
	                }
	            });
	}




	
}
