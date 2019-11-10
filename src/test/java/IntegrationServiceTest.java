import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.IntegrationEventFailureRepository;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.IntegrationEventFailureEntity;
import com.nasnav.test.integration.event.TestEvent;
import com.nasnav.test.integration.event.handler.TestEventHandler;

import net.jcip.annotations.NotThreadSafe;
import net.jodah.concurrentunit.Waiter;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
//@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Service_Test_Data_Insert.sql"})
//@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class IntegrationServiceTest {
	
	private static final Long ORG_ID = 99001L;
	public static final Long HANDLE_DELAY_MS = 1000L;
	
	
	private static final String INTEGRATION_PARAM_PREPARE_QUERY = 
			"INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());" + 
			"INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());" + 
			"INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(1, 'INTEGRATION_MODULE', TRUE);" + 
			"INSERT INTO public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at)" + 
			"VALUES(1, 1, 99001, 'com.nasnav.test.integration.modules.TestIntegrationModule', now(), now());" ;

	private static final String CLEAN_QUERY = 
			"DELETE FROM public.integration_mapping where organization_id BETWEEN 99000 AND 99999;\n" + 
			"DELETE FROM public.integration_param where  organization_id BETWEEN 99000 AND 99999;\n" + 
			"DELETE FROM public.integration_mapping_type;\n" + 
			"DELETE FROM public.integration_param_type;\n"+
			"DELETE FROM public.organizations WHERE id BETWEEN 99000 AND 99999;"; 
			
	
	@Autowired
	IntegrationService integration;
	
	
	
	@Autowired
	IntegrationEventFailureRepository eventFailureRepo;
	
	
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
	
	
	
	//push event with data [DONE]
	//push invalid null event [DONE]
	//push event with null org [DONE]
	//push event with null data 
	//on complete action fails 
	//push event to organization with no module
	//organization with integration info but null integration module? 
	//event with no handler
	//assert data is delivered to the correct event handler
	//assert event is processed asynchronously
	//assert several events are delivered in the given MAX rate 
	//test retry logic
	//test error handling after retry fails
	@Test
	public void pushEventTest() throws InterruptedException, TimeoutException {		
		
		Waiter waiter = new Waiter();
		Consumer<TestEvent> onComplete = getEventCompleteAction(waiter);
		TestEventHandler.onHandle = e -> {
								this.onEventHandle(e); 
								waiter.resume(); //notify the test thread that the event was handled
							};
		
							
		TestEvent event = new TestEvent(ORG_ID, TestEventHandler.EXPECTED_DATA);
		integration.pushIntegrationEvent(event, onComplete , this::onPushEventError);
		
		assertAsyncEventPush(event);
		
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*1.5), TimeUnit.MILLISECONDS);
	}





	private void assertAsyncEventPush(TestEvent event) {
		Duration pushDuration = Duration.between(event.getCreationTime(), LocalDateTime.now());
		System.out.println("Push Duration in Mills: " +  pushDuration.toMillis());
		
		assertTrue("event push is asynchronous ,it should be done fast, and must return before handling the event!"
				  , pushDuration.toMillis() < HANDLE_DELAY_MS);
	}
	
	
	
	
	
	
	@Test(expected = RuntimeException.class)
	public void testNullEvent() throws TimeoutException, InterruptedException {
		Waiter waiter = new Waiter();
		Consumer<TestEvent> onComplete = getEventCompleteAction(waiter);
		TestEventHandler.onHandle = e -> {
								this.onEventHandle(e); 
								waiter.resume(); //notify the test thread that the event was handled
							};
							
							
		TestEvent event = null;
		integration.pushIntegrationEvent(event, onComplete , this::expectedInvalidEventAction);
		
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*1.5), TimeUnit.MILLISECONDS);
	}
	
	
	
	
	
	
	@Test(expected = RuntimeException.class)
	public void testNullOrgEvent() throws TimeoutException, InterruptedException {
		Waiter waiter = new Waiter();
		Consumer<TestEvent> onComplete = getEventCompleteAction(waiter);
		TestEventHandler.onHandle = e -> {
								this.onEventHandle(e); 
								waiter.resume(); //notify the test thread that the event was handled
							};
							
							
		TestEvent event =  new TestEvent(null, TestEventHandler.EXPECTED_DATA);
		integration.pushIntegrationEvent(event, onComplete , this::expectedInvalidEventAction);
		
		
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*1.5), TimeUnit.MILLISECONDS);
	}
	
	
	
	
	@Test(expected = RuntimeException.class)
	public void testOnCompleteFailure() throws TimeoutException, InterruptedException {
		Long countBefore = eventFailureRepo.count();
		
		Waiter waiter = new Waiter();
		Consumer<TestEvent> onComplete = getFailingOnCompleteAction(waiter);
		TestEventHandler.onHandle = e -> {
								this.onEventHandle(e); 
								waiter.resume(); //notify the test thread that the event was handled
							};
		//--------------------------------------------------------------							
							
		TestEvent event =  new TestEvent(ORG_ID, TestEventHandler.EXPECTED_DATA);
		integration.pushIntegrationEvent(event, onComplete , this::fallbackActionThrowException);
		//--------------------------------------------------------------
		
		//wait until the event is handled in another thread until timeout
		waiter.await((long)(HANDLE_DELAY_MS*1.5), TimeUnit.MILLISECONDS);		
		//--------------------------------------------------------------
		Long countAfter = eventFailureRepo.count();
		
		assertEquals(0L, countBefore.longValue());
		assertNotEquals(1L, countAfter.longValue());
		
		IntegrationEventFailureEntity eventFailure = eventFailureRepo.findAll().get(0);
		
		assertEquals(event.getOrganizationId(), eventFailure.getOrganizationId());
		assertNotNull(eventFailure.getEventData());
		assertNotNull(eventFailure.getHandleException());
		assertNotNull(eventFailure.getFallbackException());
		assertNotNull(eventFailure.getCreatedAt());
	}
	
	

	
	
	
	private Consumer<TestEvent> getEventCompleteAction(Waiter waiter) {		
		return 
				event ->{
					waiter.assertEquals(TestEventHandler.EXPECTED_RESULT, event.getEventResult());
					
					Duration eventHandlingDuration = Duration.between(event.getCreationTime(), event.getResultRecievedTime());
					System.out.println("Handle Duration in Mills: " +  eventHandlingDuration.toMillis());
					
					waiter.assertTrue(eventHandlingDuration.toMillis() >= HANDLE_DELAY_MS);
					waiter.resume();
				};
	}
	
	
	
	
	
	
	private Consumer<TestEvent> getFailingOnCompleteAction(Waiter waiter) {		
		return 
				event ->{
					System.out.println("On Complete was called!");
					waiter.assertTrue(true);
					throw new RuntimeException();
				};
	}






	private void onEventHandle(TestEvent event) {
		try {
			Thread.sleep(HANDLE_DELAY_MS);
		} catch (InterruptedException e) {
			assertTrue(false);
		}
		
		System.out.println("Event Is Handled! result = "+  event.getEventResult() );
		assertEquals( TestEventHandler.EXPECTED_DATA, event.getEventData());
	}
	
	
	
	
	
	
	private void onPushEventError(TestEvent event, Throwable t) {
		assertTrue("integration error happended!", false);
		t.printStackTrace();
		throw new RuntimeException("Error happened during Event handling!");
	}
	
	
	
	
	
	private void expectedInvalidEventAction(TestEvent event, Throwable t) {
		System.out.println("Running on Error callback!");
		assertTrue("integration error happended!", true);
		t.printStackTrace();
		throw new RuntimeException("Error happened during Event handling!");
	}
	
	
	
	
	private void fallbackActionThrowException(TestEvent event, Throwable t) {
		System.out.println("Running on Error callback!");
		assertTrue("integration error happended!", true);
		t.printStackTrace();
		throw new RuntimeException("Error happened during Event handling!");
	}
}
