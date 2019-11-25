package com.nasnav.integration;

import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EVENT_HANDLE_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EVENT_HANDLE_FALLBACK_RUN_FAILED;

import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.EventResult;

import reactor.core.publisher.Mono;






public abstract class IntegrationEventListener<E extends Event<T,R>, T, R> {
	protected Logger logger;
	protected IntegrationService integrationService; 
	
	public IntegrationEventListener(@Nonnull IntegrationService integrationService) {
		this.integrationService = integrationService;
		logger = Logger.getLogger(this.getClass().getName());
	}	
	
	
	
	
	
	
	
	public void pushEvent(E event, BiConsumer<E, Throwable> onError) {
		BiConsumer<E,Throwable> onErrorWrapper = wrapOnErrorCallback(event, onError);						
		try {
			
			Mono<R> resultDataMono = handleEventAsync( event.getEventInfo() );			
			resultDataMono.subscribe( event::broadcastResultData );
			
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
	
	
	
	
	
	
	
	private BiConsumer<E,Throwable> wrapOnErrorCallback(E event, BiConsumer<E, Throwable> onError){
		return (e,t)-> {
					this.handleError(e, t);
					onError.accept(e, t);
				}; 
	}

	
	
	
	/**
	 * @param event to be handled
	 * @return after being handled successfully, a mono of the eventResult. 
	 * */
	protected abstract Mono<R> handleEventAsync(EventInfo<T> event);	
	
	
	
	
	
	/**
	 * Fallback logic for handling events.
	 * This should be called when handleEvent() fails to handle the event.
	 * @param event Event that caused an error
	 * @return after handling the event return the event with the result saved inside it. 
	 * */
	protected abstract E handleError(E event, Throwable t);	
}
