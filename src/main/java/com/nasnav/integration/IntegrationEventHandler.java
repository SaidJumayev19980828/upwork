package com.nasnav.integration;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.IntegrationService;


public abstract class IntegrationEventHandler<E extends Event<T,R>, T, R> {
	
	protected IntegrationService integrationService; 
	
	public IntegrationEventHandler(@Nonnull IntegrationService integrationService) {
		this.integrationService = integrationService;
	}	
	
	
	
	
	public void pushEvent(E event, Consumer<E> onComplete, BiConsumer<E, Throwable> onError) {
		Consumer<E> onCompleteWrapper = wrapOnCompleteCallback(event, onComplete);
		BiConsumer<E,Throwable> onErrorWrapper = wrapOnErrorCallback(event, onError);						
				
		handleEventAsync(event, onCompleteWrapper, onErrorWrapper);
	}
	
	
	
	
	
	private Consumer<E> wrapOnCompleteCallback(E event, Consumer<E> onComplete){
		return e ->{
					onComplete.accept(event);
					event.setResultRecievedTime(LocalDateTime.now());
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
