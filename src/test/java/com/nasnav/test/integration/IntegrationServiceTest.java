package com.nasnav.test.integration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;



import static com.nasnav.integration.enums.MappingType.PRODUCT;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.util.Pair;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.IntegrationEventFailureRepository;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.IntegrationEventFailureEntity;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.integration.event.HandlingInfo;
import com.nasnav.test.integration.event.TestEvent;
import com.nasnav.test.integration.event.TestEvent2;
import com.nasnav.test.integration.event.TestEventWithHandlerInfo;
import com.nasnav.test.integration.event.TestEventWithNoHandler;
import com.nasnav.test.integration.event.handler.HandlingInfoSaver;
import com.nasnav.test.integration.event.handler.TestEvent2Handler;
import com.nasnav.test.integration.event.handler.TestEventHandler;

import net.jcip.annotations.NotThreadSafe;
import net.jodah.concurrentunit.Waiter;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
public class IntegrationServiceTest {
	
	private static final String MAPPING_REMOTE_VAL = "REMOTE_VAL";
	private static final String MAPPING_LOCAL_VAL = "LOCAL_VAL";
	private static final Long ORG_ID = 99001L;
	private static final Long NO_INTEGRATION_ORG_ID = 99002L;
	private static final Long ANOTHER_ORG_ID = 99003L;
	public static final Long HANDLE_DELAY_MS = 1000L;
	
	
	private static final String INTEGRATION_PARAM_PREPARE_QUERY = 
			"INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());" + 
			"INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());" + 
			"INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99003, 'organization_3', now(), now());" +
			"INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(1, 'INTEGRATION_MODULE', TRUE);" + 
			"INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(2, 'MAX_REQUESTS_PER_SECOND', TRUE);" +
			"INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(3, 'EXISTING_PARAM', FALSE);"+
			"INSERT INTO public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at)" + 
			"VALUES(1, 1, 99001, 'com.nasnav.test.integration.modules.TestIntegrationModule', now(), now());\n" +
			"INSERT INTO public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at)" + 
			"VALUES(2, 2, 99001, '10', now(), now());" +
			"INSERT INTO public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at)" + 
			"VALUES(3, 1, 99003, 'com.nasnav.test.integration.modules.TestIntegrationModule', now(), now());\n" +
			"INSERT INTO public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at)" + 
			"VALUES(4, 2, 99003, '5', now(), now());" +
			"insert into public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at)\n" + 
			"values(55001, 3, 99001, 'old_val', now(), now());";

	private static final String CLEAN_QUERY = 
			"DELETE FROM public.integration_event_failure where organization_id BETWEEN 99000 AND 99999;\n"+
			"DELETE FROM public.integration_mapping where organization_id BETWEEN 99000 AND 99999;\n" + 
			"DELETE FROM public.integration_param where  organization_id BETWEEN 99000 AND 99999;\n" + 
			"DELETE FROM public.integration_mapping_type;\n" + 
			"DELETE FROM public.integration_param_type;\n" +
			"DELETE FROM public.organizations WHERE id BETWEEN 99000 AND 99999;";
	
		 
			
	
	@Autowired
	IntegrationService integration;
	
	
	
	@Autowired
	IntegrationEventFailureRepository eventFailureRepo;
	
	
	@Autowired
	IntegrationMappingRepository mappingRepo;
	
	
	/**
	 * The the loading of the Integration modules actually runs when spring context is 
	 * initialized. Which happens before @Sql annotations are executed, and
	 * so, we need to add the Integration parameters in the database before the whole test 
	 * class is loaded.
	 * */
	@BeforeClass
	public static void initIntegrationParameters() {
		TestCommons.getJdbi().useHandle( h-> h.execute(INTEGRATION_PARAM_PREPARE_QUERY));
	}
	
	
	
	
	
	@AfterClass
	public static void cleanTables() {
		TestCommons.getJdbi().useHandle( h-> h.execute(CLEAN_QUERY));
	}
	
	
	
	
	@Before
	public void clearEventHandler() {
		TestEventHandler.onHandle = null;
	}
	
	
	
	
	@Test
	public void moduleLoadingTest() {
		assertTrue("Modules are loaded after spring loads IntegrationService into the context, if it fails the application won't start!",true);
		assertNotNull( integration.getIntegrationModule(ORG_ID));
	}
	
	
	

	
	@Test
	public void pushEventTest() throws Throwable{		
		Waiter waiter = new Waiter();
		
		TestEventHandler.onHandle = getEventHandlerWithDelay() ;
		AtomicReference<Boolean> isCallbackExecuted = pushEvent(TestEvent.class, ORG_ID, TestEventHandler.EXPECTED_DATA, waiter ,TestEventHandler.EXPECTED_RESULT, true);
		
		
		//--------------------------------------------------------------
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*20), TimeUnit.MILLISECONDS);
		
		//--------------------------------------------------------------
        assertTrue("onComplete callback should be called and completed with no errors", isCallbackExecuted.get());
	}





	private <E extends Event> BiConsumer<E, Throwable> getUnexpectedErrorCallback(Waiter waiter) {
		return (e,t) -> waiter.assertFalse(true);
	}
	
	
	
	
	private <T,R> Consumer<EventResult<T,R>> getUnexpectedCallback(Waiter waiter) {
		return (e) -> {
			waiter.assertFalse(true);
			waiter.resume();
		};
	}
	
	
	
	
	
	private <T> Consumer<EventInfo<T>> getUnexpectedEventConsumer(Waiter waiter) {
		return (e) -> {
			waiter.assertFalse(true);
			waiter.resume();
		};
	}





	private <E extends Event> void assertAsyncEventPush(E event) {
		Duration pushDuration = Duration.between(event.getCreationTime() , LocalDateTime.now());
		System.out.println("Push Duration in Mills: " +  pushDuration.toMillis());
		
		assertTrue("event push is asynchronous ,it should be done fast, and must return before handling the event!"
				  , pushDuration.toMillis() < HANDLE_DELAY_MS);
	}
	
	
	
	
	
	
	
	@Test(expected = TimeoutException.class)
	public void pushEventForOrgWithNoIntegrationTest() throws InterruptedException, TimeoutException, InvalidIntegrationEventException {		
		
		AtomicReference<Boolean> isCalled = new AtomicReference<Boolean>();
		isCalled.set(false);
		
		//the callbacks should never be called, and exception will be thrown on validating the event
		Waiter waiter = new Waiter();
		Consumer<EventResult<String, String>> onComplete = getUnexpectedCallback(waiter);
		BiConsumer<TestEvent, Throwable> onError = (e,t) -> waiter.assertTrue(false);
		TestEventHandler.onHandle = getUnexpectedEventConsumer(waiter);
		
		//--------------------------------------------------------------		
							
		TestEvent event = new TestEvent(NO_INTEGRATION_ORG_ID, TestEventHandler.EXPECTED_DATA, onComplete);
		integration.pushIntegrationEvent(event, onError);
	
		//--------------------------------------------------------------
		//wait until timeout, because none of the callbacks was called
		waiter.await((long)(HANDLE_DELAY_MS*10), TimeUnit.MILLISECONDS);
	}
	
	
	
	
	
	
	
	@Test(expected = TimeoutException.class)
	public void pushEventWithNoHandlerTest() throws InterruptedException, TimeoutException, InvalidIntegrationEventException {		
		
		AtomicReference<Boolean> isCalled = new AtomicReference<Boolean>();
		isCalled.set(false);
		
		//the callbacks should never be called, and exception will be thrown on validating the event
		Waiter waiter = new Waiter();
		Consumer<EventResult<String, String>> onComplete = getUnexpectedCallback(waiter);
		BiConsumer<TestEventWithNoHandler, Throwable> onError = (e,t) -> waiter.assertTrue(false);
		TestEventHandler.onHandle = getUnexpectedEventConsumer(waiter);
		
		//--------------------------------------------------------------		
							
		TestEventWithNoHandler event = new TestEventWithNoHandler(ORG_ID, TestEventHandler.EXPECTED_DATA, onComplete);
		integration.pushIntegrationEvent(event, onError);
	
		//--------------------------------------------------------------
		//wait until timeout, because none of the callbacks was called
		waiter.await((long)(HANDLE_DELAY_MS*10), TimeUnit.MILLISECONDS);
	}
	
	
	
	
	
	@Test
	public void pushMultipleDifferentEventsTest() throws Throwable{		
		Waiter waiter = new Waiter();
		
		TestEventHandler.onHandle = getEventHandlerWithDelay() ;
		AtomicReference<Boolean> callBackIsCalled1 = pushEvent(TestEvent.class, ORG_ID, TestEventHandler.EXPECTED_DATA, waiter ,TestEventHandler.EXPECTED_RESULT, false);
		
		
		TestEvent2Handler.onHandle = getEventHandlerWithDelay() ;
		AtomicReference<Boolean> callBackIsCalled2 = pushEvent(TestEvent2.class, ORG_ID, TestEvent2Handler.EXPECTED_DATA, waiter, TestEvent2Handler.EXPECTED_RESULT, true);
		
		
		//--------------------------------------------------------------
		//wait until the event is handled in another thread or until timeout
		waiter.await((long)(HANDLE_DELAY_MS*20), TimeUnit.MILLISECONDS);
		
		//--------------------------------------------------------------
        assertTrue("onComplete callback should be called and completed with no errors for both events"
        				, callBackIsCalled1.get() && callBackIsCalled2.get() );
				
	}





	private <E extends Event<T, R>, T,R> AtomicReference<Boolean> pushEvent(Class<E> eventClass, Long orgId, T eventData, Waiter waiter, Object expectedResult, boolean resumeTestAfterThis) throws InvalidIntegrationEventException, TimeoutException, InterruptedException, Exception, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		AtomicReference<Boolean> isCalled = new AtomicReference<Boolean>();
		isCalled.set(false);
				
		Consumer<EventResult<T,R>> onComplete = getEventCompleteAction(waiter, isCalled, expectedResult, resumeTestAfterThis);
				
		E event = eventClass.getConstructor(Long.class, eventData.getClass(), Consumer.class)							
							.newInstance(orgId, eventData, onComplete);
		BiConsumer<E, Throwable> onError = getUnexpectedErrorCallback(waiter);		
		//--------------------------------------------------------------		
							
		integration.pushIntegrationEvent(event , onError);
		//--------------------------------------------------------------
		assertAsyncEventPush(event);
		
		//--------------------------------------------------------------
		return isCalled;		
	}
	
	
	
	
	
	
	
	@Test(expected = InvalidIntegrationEventException.class)
	public void testNullEvent() throws TimeoutException, InterruptedException, InvalidIntegrationEventException {
		Waiter waiter = new Waiter();
		
		//the callbacks should never be called, and exception will be thrown on validating the event
		Consumer<TestEvent> onComplete = e -> waiter.assertTrue(false);
		BiConsumer<TestEvent, Throwable> onError = (e,t) -> waiter.assertTrue(false);
		TestEventHandler.onHandle = e -> {
								waiter.assertTrue(false);
								waiter.resume(); //notify the test thread that the event was handled
							};
							
		//--------------------------------------------------------------
		TestEvent event = null;
		integration.pushIntegrationEvent(event, onError);
		
		//--------------------------------------------------------------
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*10), TimeUnit.MILLISECONDS);
	}
	
	
	
	
	
	
	
	@Test(expected = InvalidIntegrationEventException.class)
	public void testNullOrgEvent() throws TimeoutException, InterruptedException, InvalidIntegrationEventException {
		
		Waiter waiter = new Waiter();		
		//the callbacks should never be called, and exception will be thrown on validating the event
		Consumer<EventResult<String, String>> onComplete = e -> waiter.assertTrue(false);
		BiConsumer<TestEvent, Throwable> onError = (e,t) -> waiter.assertTrue(false);
		TestEventHandler.onHandle = e -> {
								waiter.assertTrue(false);
								waiter.resume(); //notify the test thread that the event was handled
							};
							
		//--------------------------------------------------------------					
		TestEvent event =  new TestEvent(null, TestEventHandler.EXPECTED_DATA, onComplete);
		integration.pushIntegrationEvent(event , onError);		
		
		//--------------------------------------------------------------
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*10), TimeUnit.MILLISECONDS);
	}
	
	
	
	
	
	@Test
	public void testTotalFailure() throws TimeoutException, InterruptedException, InvalidIntegrationEventException {
		Long countBefore = eventFailureRepo.count();
		
		Waiter waiter = new Waiter();
		Consumer<EventResult<String, String>> onComplete = getFailingOnCompleteAction(waiter);
		TestEventHandler.onHandle = e -> {	this.onEventHandle(e);	};
		//--------------------------------------------------------------							
							
		TestEvent event =  new TestEvent(ORG_ID, TestEventHandler.EXPECTED_DATA, onComplete);
		integration.pushIntegrationEvent(event, this::fallbackActionThrowException);
		//--------------------------------------------------------------
		
		//wait until the event is handled in another thread until timeout
		Thread.sleep((long) (HANDLE_DELAY_MS*20));		
		//--------------------------------------------------------------
		assertEventFailureSavedToDB(countBefore, event);
	}





	private void assertEventFailureSavedToDB(Long countBefore, TestEvent event) {
		Long countAfter = eventFailureRepo.count();
		
		assertEquals(0L, countBefore.longValue());
		assertEquals(1L, countAfter.longValue());
		
		IntegrationEventFailureEntity eventFailure = eventFailureRepo.findAll().get(0);
		
		assertEquals(event.getOrganizationId(), eventFailure.getOrganizationId());
		assertNotNull(eventFailure.getEventData());
		assertNotNull(eventFailure.getHandleException());
		assertNotNull(eventFailure.getFallbackException());
		assertNotNull(eventFailure.getCreatedAt());
	}
	
	
	
	
	
	
	@Test
	public void onCompleteActionFailsTest() throws InterruptedException, TimeoutException, InvalidIntegrationEventException {		
		
		AtomicReference<Boolean> isCalled = new AtomicReference<Boolean>();
		isCalled.set(false);
		
		Waiter waiter = new Waiter();
		Consumer<EventResult<String,String>> onComplete = getFailingOnCompleteAction(waiter);
		BiConsumer<TestEvent, Throwable> onError = (e,t) -> {
														System.out.println("Running Error callback!");
														isCalled.set(true);
														waiter.resume();
													};
		TestEventHandler.onHandle = e -> {
								this.onEventHandle(e); 
							};
		//--------------------------------------------------------------		
							
		TestEvent event = new TestEvent(ORG_ID, TestEventHandler.EXPECTED_DATA, onComplete);
		integration.pushIntegrationEvent(event, onError);
		//--------------------------------------------------------------
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*20), TimeUnit.MILLISECONDS);
		
		//--------------------------------------------------------------
		assertTrue(isCalled.get());		
	}
	
	

	
	
	
	private <T,R> Consumer<EventResult<T, R>> getEventCompleteAction(Waiter waiter, AtomicReference<Boolean> isCalled, Object ExpectedResult, boolean resumeTestAfterThis) {		
		Consumer<EventResult<T, R>> commonCompleteAction = getEventCompleteAction(waiter, isCalled, resumeTestAfterThis, HANDLE_DELAY_MS);
		return 
				res ->{
					waiter.assertEquals(ExpectedResult, res.getReturnedData());					
					commonCompleteAction.accept(res);					
				};
	}
	
	
	
	
	
	
	private <T,R> Consumer<EventResult<T,R>> getEventCompleteAction(Waiter waiter, AtomicReference<Boolean> isCalled, boolean resumeTestAfterThis, Long minHandleDelayMillis) {		
		return 
				res ->{
					isCalled.set(true);					
					Long duration = getDurationMillis(res);					
					printEventCallbackInfo(res, duration);					
					waiter.assertTrue( duration >= minHandleDelayMillis);									
					
					if(resumeTestAfterThis)
					{	
						waiter.resume();
					}
				};
	}





	private <T,R> void printEventCallbackInfo(EventResult<T,R> res, Long duration) {
		System.out.println( String.format(">>> On Complete was called for event of type[%s]!", res.getEventInfo().getClass()));
		System.out.println(
				String.format(">>> HandleDuration in Mills[%d] for event of type[%s]" , duration ,res.getEventInfo().getClass()) );
		System.out.println(
				String.format(">>> Running on thread [%s]" , Thread.currentThread()) );
	}



	
	
	private <T,R> Long getDurationMillis(EventResult<T,R> result) {
		return Duration.between(result.getEventInfo().getCreationTime(), result.getResultRecievedTime()).toMillis();
	}
	
	
	
	
	private Consumer<EventResult<String, String>> getFailingOnCompleteAction(Waiter waiter) {		
		return 
				res ->{
					System.out.println("On Complete was called!");
					throw new RuntimeException("Event Complete callback is throwing an Exception!!");
				};
	}






	private  void onEventHandle(EventInfo event) {
		getEventHandlerWithDelay().accept(event);
	}
	
	
	
	
	private <T> Consumer<EventInfo<T>> getEventHandlerWithDelay(){
		return event -> {
			try {
				Thread.sleep(HANDLE_DELAY_MS);
			} catch (InterruptedException e) {
				assertTrue(false);
			}
			
			System.out.println(
					String.format("Event Is Handled for event of type[%s]!", event.getClass().getName() ) );
		};
	}
	
	
	
	
	private void fallbackActionThrowException(TestEvent event, Throwable t) {
		System.out.println("Running on Error callback!");
		t.printStackTrace();
		throw new RuntimeException("Error happened during Event error fallback!");
	}
	
	
	
	
	
	//assert several events are delivered in the given MAX rate
	/**
	 * - set rate parameter in the database
	 * Set<thread>
	 * List<isCalledflage>
	 * Set<event> afterhandling
	 * loop
	 * 	- create on complete action
	 * 		- save handling start time to (event)
	 * 		- have some delay
	 * 		- show message to sys out
	 * 		- show current thread
	 * 		- save (thread, is called)
	 * 		- save event
	 * 		- last on complete calls waiter.resume
	 * 	- push event
	 * 
	 * - wait the actions to finish
	 * - assert:
	 * 		- diff between every handling start time >= sampling time
	 * 		- set of threads size > 1 (the events where handled on multiple threads)
	 * 		- all on complete actions were called 
	 * 		- no error callback were called
	 * 		- event_failure table is empty
	 * @throws InvalidIntegrationEventException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * */
	
	@Test
	public void testHandlingEventWithRate() throws InvalidIntegrationEventException, TimeoutException, InterruptedException {
		Waiter waiter = new Waiter();
		Integer eventsNum = 100;
		Long expectedEventRate = 10L;			
		Long orgId = ORG_ID;
		
		List<HandlingInfo> orgEvents = pushBulkOfEvents(waiter, eventsNum, orgId, true);
		//--------------------------------------------------------------
		Long awaitTime = (long) (HandlingInfoSaver.HANDLING_TIME*(eventsNum/expectedEventRate)*10);
		waiter.await( awaitTime );
		
		//--------------------------------------------------------------
		assertEventsAreHandledWithRate(expectedEventRate, orgEvents);
	}
	
	
	
	
	@Test
	public void testHandlingMultipleOrgEventWithRate() throws InvalidIntegrationEventException, TimeoutException, InterruptedException {
		Waiter waiter = new Waiter();
		Integer eventsNum1 = 100;
		Long expectedEventRate1 = 10L;			
		Long orgId1 = ORG_ID;
		
		List<HandlingInfo> org1Events = pushBulkOfEvents(waiter, eventsNum1, orgId1, false);
		
		
		Integer eventsNum2 = 50;
		Long expectedEventRate2 = 5L;			
		Long orgId2 = ANOTHER_ORG_ID;
		
		List<HandlingInfo> org2Events = pushBulkOfEvents(waiter, eventsNum2, orgId2, true);
		
		//--------------------------------------------------------------
		Integer eventsNum = eventsNum1 + eventsNum2;
		Long expectedEventRate = Math.min(expectedEventRate1, expectedEventRate2);
		Long awaitTime = (long) (HandlingInfoSaver.HANDLING_TIME*(eventsNum/expectedEventRate)*20);
		waiter.await( awaitTime );
		
		//--------------------------------------------------------------
		assertEventsAreHandledWithRate(expectedEventRate1, org1Events);
		assertEventsAreHandledWithRate(expectedEventRate2, org2Events);
	}





	private List<HandlingInfo> pushBulkOfEvents(Waiter waiter, Integer eventsNum, Long orgId, Boolean resumeTestThread)
			throws InvalidIntegrationEventException, TimeoutException, InterruptedException {
		List<HandlingInfo> events = new ArrayList<>();
		
		for(int i = 0; i < eventsNum ; i++) {
			pushSingleEventWithIndex(i, eventsNum, orgId, waiter, events, resumeTestThread);			
		}
		
		return events;
	}





	private void pushSingleEventWithIndex(int i, Integer eventsNum , Long orgId, Waiter waiter,
			List<HandlingInfo> events, Boolean resumeTestThread) throws InvalidIntegrationEventException {
		
		AtomicReference<Boolean> isCalled = new AtomicReference<>(false);
		boolean resumeAfterThis = resumeTestThread && (i == eventsNum -1);
		
		Consumer<EventResult<Integer,HandlingInfo>> onComplete = getEventCompleteAction(waiter, isCalled, resumeAfterThis, HandlingInfoSaver.HANDLING_TIME);
		
		Consumer<EventResult<Integer,HandlingInfo>> onCompleteWrapper = 
				res ->{						
					res.getReturnedData().setCallBackExecuted(true);
					events.add(res.getReturnedData());
					System.out.println( String.format("Event#%d for org[%d] was handled!", res.getEventInfo().getEventData(), res.getEventInfo().getOrganizationId()));
					onComplete.accept(res);
				};
				
		BiConsumer<TestEventWithHandlerInfo, Throwable> onError = getUnexpectedErrorCallback(waiter);		
		TestEventWithHandlerInfo event = new TestEventWithHandlerInfo(orgId, i, onCompleteWrapper);
		//--------------------------		
							
		integration.pushIntegrationEvent(event, onError);
		//--------------------------
		assertAsyncEventPush(event);
	}





	private void assertEventsAreHandledWithRate(Long expectedEventRate, List<HandlingInfo> handlingInfo) {
		Long delay = (long) ((1.0/expectedEventRate)*1000);	
		
		IntStream.range(1, handlingInfo.size())
				.mapToObj(i -> Pair.of(handlingInfo.get(i-1), handlingInfo.get(i)))
				.map(this::durationBetweenHandlingStartTime)
				.forEach(d -> System.out.println(">> Duration between startHandling : " + d));
		
		Boolean allEventsHandled = areAllEventsHandled(handlingInfo);		
		Long threadsUsedNum = getDistinctThreadCount(handlingInfo);		
		Long failureEvents = eventFailureRepo.count();		
		Boolean eventsAreSampled = areEventsSampledWithPeriod(handlingInfo, delay);
		
		System.out.println(">>> Number of used threads : " + threadsUsedNum);
		
		assertTrue(allEventsHandled);
		assertNotEquals(1L, threadsUsedNum.longValue());
		assertEquals(0L, failureEvents.longValue());
		assertTrue("events are not delayed by["+ delay +"]!"
				+ ", if events are delayed, they will not be emitted and handled at the same time, "
				+ "but each event is emitted after sometime from emitting of the last event"
				, eventsAreSampled);
	}





	private Boolean areAllEventsHandled(List<HandlingInfo> handlingInfo) {
		Boolean allEventsHandled = handlingInfo.stream().allMatch(HandlingInfo::getCallBackExecuted);
		return allEventsHandled;
	}





	private Long getDistinctThreadCount(List<HandlingInfo> handlingInfo) {
		Long threadsUsedNum = handlingInfo.stream()
											.map(HandlingInfo::getThread)
											.distinct()
											.count();
		return threadsUsedNum;
	}





	private Boolean areEventsSampledWithPeriod(List<HandlingInfo> handlingInfo, Long samplePeriod) {
		Duration samplePeriodDur = Duration.ofMillis(samplePeriod);
		Boolean eventsAreSampled = IntStream.range(1, handlingInfo.size())
											.mapToObj(i -> Pair.of(handlingInfo.get(i-1), handlingInfo.get(i)))
											.map(this::durationBetweenHandlingStartTime)
											.allMatch(duration -> isEqualToDurationWithinMargin(duration, samplePeriodDur, 10L));
		return eventsAreSampled;
	}
	
	
	
	
	
	private Duration durationBetweenHandlingStartTime(Pair<HandlingInfo, HandlingInfo> pair) {
		HandlingInfo event = pair.getFirst();
		HandlingInfo nextEvent = pair.getSecond();
		return Duration.between(event.getHandlingStartTime() , nextEvent.getHandlingStartTime());
	}
	
	
	
	
	
	private Boolean isEqualToDurationWithinMargin(Duration duration, Duration expected, Long marginMillis) {
		
		return duration.compareTo(expected.minusMillis(marginMillis)) >= 0
				 && duration.compareTo(expected.plusMillis(marginMillis)) < 0;
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Mapping.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Integration_Test_cleanup.sql"})
	public void testAddIntegrationMapping() throws BusinessException {
		assertMappingsExist();
		
		//-----------------------------------------------------------
		integration.addMappedValue(ORG_ID, MappingType.PRODUCT, MAPPING_LOCAL_VAL, MAPPING_REMOTE_VAL);
		
		//-----------------------------------------------------------
		assertMappingSaved();
		
		assertEquals("the previous mappings of the remote and local values should be delete, and only the new mapping should"
				+ "exists"
				, 1L
				, mappingRepo.count());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Mapping.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Integration_Test_cleanup.sql"})
	public void testDeleteIntegrationMapping() throws BusinessException {
		assertMappingsExist();
		
		//-----------------------------------------------------------
		integration.deleteMappingByLocalValue(ORG_ID, MappingType.PRODUCT, MAPPING_LOCAL_VAL);
		assertEquals(1 , mappingRepo.count());
		
		integration.deleteMappingByRemoteValue(ORG_ID, MappingType.PRODUCT, MAPPING_REMOTE_VAL);
		assertEquals(0 , mappingRepo.count());		
	}
	
	
	
	
	
	
	
	@Test
	public void testGetIntegrationParam() throws BusinessException {
		String paramName = "EXISTING_PARAM";
		String paramValue = integration.getIntegrationParamValue(ORG_ID, paramName);
		
		assertEquals("old_val", paramValue);
	}
	
	
	
	
	@Test
	public void testGetIntegrationParamNonExistingParam() throws BusinessException {
		String paramName = "NON_EXISTING_PARAM";
		String paramValue = integration.getIntegrationParamValue(ORG_ID, paramName);
		
		assertNull(paramValue);
	}
	
	
	
	
	
	@Test
	public void testGetIntegrationParamOrgWithNoIntegration() throws BusinessException {
		String paramName = "EXISTING_PARAM";
		String paramValue = integration.getIntegrationParamValue(99002L, paramName);
		
		assertNull(paramValue);
	}
	
	





	private void assertMappingSaved() {
		Optional<IntegrationMappingEntity> mapping = mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, MappingType.PRODUCT.getValue(), MAPPING_LOCAL_VAL);
		
		assertTrue(mapping.isPresent());
		assertEquals(MAPPING_LOCAL_VAL, mapping.get().getLocalValue());
		assertEquals(MAPPING_REMOTE_VAL, mapping.get().getRemoteValue());
		assertNotNull( mapping.get().getCreatedAt() );
		assertNotNull( mapping.get().getUpdatedAt() );
	}





	private void assertMappingsExist() {		
		boolean localValueMappingExists = mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(
															ORG_ID, MappingType.PRODUCT.getValue(), MAPPING_LOCAL_VAL)
													 .isPresent();		
		boolean remoteValueMappingExists = mappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(
															ORG_ID, MappingType.PRODUCT.getValue(), MAPPING_REMOTE_VAL)
													  .isPresent();		
		assertTrue("Only one local value can exists for the same org and mapping type."
					+ " a mapping is already inserted for test values."
					, localValueMappingExists);
		
		assertTrue("Only one remote value can exists for the same org and mapping type."
					+ " a mapping is already inserted for test values."
					, remoteValueMappingExists);
		
		assertEquals("only two mapping exists at the beginning of the test", 2L, mappingRepo.count());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Mapping.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Integration_Test_cleanup.sql"})
	public void testGetRemoteMappedValue() {
		String remoteVal = integration.getRemoteMappedValue(ORG_ID, PRODUCT, MAPPING_LOCAL_VAL);		
		assertEquals("OLD_REMOTE_VAL" , remoteVal);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Mapping.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Integration_Test_cleanup.sql"})
	public void testGetRemoteMappedValueNonExisting() {
		String remoteVal = integration.getRemoteMappedValue(ORG_ID, PRODUCT, "NON_EXISTING_VAL");		
		assertNull("OLD_REMOTE_VAL" , remoteVal);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Mapping.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Integration_Test_cleanup.sql"})
	public void testGetLocalMappedValue() {
		String localVal = integration.getLocalMappedValue(ORG_ID, PRODUCT, MAPPING_REMOTE_VAL);		
		assertEquals("OLD_LOCAL_VAL" , localVal);
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Mapping.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Integration_Test_cleanup.sql"})
	public void testGetLocalMappedValueNonExisting() {
		String localVal = integration.getLocalMappedValue(ORG_ID, PRODUCT, "NON_EXISTING_VAL");		
		assertNull(localVal);
	}
	
}
