package com.nasnav.integration;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;


import com.nasnav.integration.events.Event;
import com.nasnav.integration.IntegrationService;




import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EVENT_HANDLE_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EVENT_HANDLE_FALLBACK_RUN_FAILED;






public abstract class IntegrationEventHandler<E extends Event<T,R>, T, R> {
	protected Logger logger;
	protected IntegrationService integrationService; 
	
	public IntegrationEventHandler(@Nonnull IntegrationService integrationService) {
		this.integrationService = integrationService;
		logger = Logger.getLogger(this.getClass().getName());
	}	
	
	
	
	
	public void pushEvent(E event, Consumer<E> onComplete, BiConsumer<E, Throwable> onError) {
		Consumer<E> onCompleteWrapper = wrapOnCompleteCallback(event, onComplete);
		BiConsumer<E,Throwable> onErrorWrapper = wrapOnErrorCallback(event, onError);						
		try {
			handleEventAsync(event, onCompleteWrapper, onErrorWrapper);
		}catch(Throwable t) {
			logger.log(Level.SEVERE 
					,String.format( ERR_EVENT_HANDLE_FAILED, event, t.getClass()) 
					,t);
			try {
				onErrorWrapper.accept(event, t);
			}catch(Throwable t2){
				logger.log(Level.SEVERE 
								,String.format( ERR_EVENT_HANDLE_FALLBACK_RUN_FAILED, event, t.getClass()) 
								,t2);
				
				integrationService.runGeneralErrorFallback(event, t, t2);
			}
			
		}
	}
	
	
	
	
	
	private Consumer<E> wrapOnCompleteCallback(E event, Consumer<E> onComplete){
		return e ->{
					event.setResultRecievedTime(LocalDateTime.now());
					onComplete.accept(event);					
				};
	}
	
	
	
	
	
	private BiConsumer<E,Throwable> wrapOnErrorCallback(E event, BiConsumer<E, Throwable> onError){
		return (e,t)-> {
					this.handleError(e, t);
					onError.accept(e, t);
				}; 
	}

	
	
	
	/**
	 * @param event Event to be handled
	 * @return after being handled successfully, return the event with the result saved inside it. 
	 * */
	protected abstract void handleEventAsync(E event, Consumer<E> onComplete, BiConsumer<E, Throwable> onError);	
	
	
	
	
	
	/**
	 * Fallback logic for handling events.
	 * This should be called when handleEvent() fails to handle the event.
	 * @param event Event that caused an error
	 * @return after handling the event return the event with the result saved inside it. 
	 * */
	protected abstract E handleError(E event, Throwable t);	
}
