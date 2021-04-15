package com.nasnav.integration.events;

import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.nasnav.exceptions.ErrorCodes.INTG$EVENT$0001;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.NONE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Data
public abstract class Event<T, R> {
	protected static Logger logger = LogManager.getLogger();

	protected EventInfo<T> eventInfo;
	@Getter
	protected Mono<EventResult<T,R>> eventResult;
	protected FluxSink<EventResult<T,R>> eventResultFluxSink;

	@Getter
	@Setter(NONE)
	protected Integer retryCount;
	

	protected Event(){
		retryCount = 0;
	}


	
	/**
	 * sinks the result data into the EventResult Mono, which notifies any subscriber to this Mono.
	 * */
	public void broadcastResultData(R resultData) {
		EventResult<T,R> evResult = new EventResult<>(eventInfo, resultData);
		eventResultFluxSink.next(evResult);
	}

	
	
	public void completeEvent() {
		eventResultFluxSink.complete();
	}
	

	
	public Long getOrganizationId() {
		return eventInfo.getOrganizationId();
	}
	
	
	
	public LocalDateTime getCreationTime() {
		return eventInfo.getCreationTime();
	}

	
	
	public void incrementRetryCount() {
		retryCount++;
	}


	public static <E extends Event<T,R>, T,R> EventBuilder<E,T,R> ofType(Class<E> eventClass){
		return new EventBuilder<E,T,R>(eventClass);
	}


	public static  class EventBuilder<E extends Event<T, R>, T, R> {
		private final E event;
		private BiConsumer<E, Throwable> defaultErrorHandler;

		private EventBuilder(Class<E> eventClass){
			if(Objects.isNull(eventClass)){
				throw new IllegalStateException("Null event type provided!");
			}
			try {
				event = eventClass.getConstructor().newInstance();
				initResultMono(FunctionalUtils::doNothing, defaultErrorHandler, event);
			} catch (Exception e) {
				logger.error(e,e);
				throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, INTG$EVENT$0001, eventClass.getName());
			}
		}



		public EventBuilder<E,T,R> setEventInfo(Long orgId , T eventData){
			var eventInfo = new EventInfo<>(orgId, eventData);
			event.setEventInfo(eventInfo);
			return this;
		}



		public EventBuilder<E,T,R> subscribeToResult(Consumer<EventResult<T, R>> onSuccess, BiConsumer<E,Throwable> onError){
			initResultMono(onSuccess, onError, event);
			return this;
		}


		public EventBuilder<E,T,R> subscribeToResult(Consumer<EventResult<T, R>> onSuccess){
			initResultMono(onSuccess, null, event);
			return this;
		}


		public E build(){
			return event;
		}


		/**
		 * initialize a hot mono the will publish the result when it is handled by the event listeners.
		 * The mono will execute the onSuccess action, and if it fails, onError will be executed.
		 * The default onSuccess do nothing, and the default onError just logs the exception.
		 * */
		private void initResultMono(Consumer<EventResult<T, R>> onSuccess, BiConsumer<E, Throwable> onError, E event) {
			EmitterProcessor<EventResult<T,R>> emitterProcessor = EmitterProcessor.create();
			event.eventResult = emitterProcessor
					.publish()
					.autoConnect()
					.next();
			var successAction = ofNullable(onSuccess).orElse(FunctionalUtils::doNothing);
			defaultErrorHandler = (e, t) -> logger.error(t, t);
			var onErrorAction = ofNullable(onError).orElse(defaultErrorHandler);
			event.eventResult.subscribe(successAction, t -> onErrorAction.accept(event, t));

			event.eventResultFluxSink = emitterProcessor.sink();
		}
	}
}
