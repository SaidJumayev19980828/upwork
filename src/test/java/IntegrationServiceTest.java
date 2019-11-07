import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.Event;
import com.nasnav.test.integration.event.TestEvent;
import com.nasnav.test.integration.event.handler.TestEventHandler;

import net.jcip.annotations.NotThreadSafe;

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
	
	
	
	
	@Test
	public void moduleLoadingTest() {
		assertTrue("Modules are loaded after spring loads IntegrationService into the context, if it fails the application won't start!",true);
		assertNotNull( integration.getIntegrationModule(ORG_ID));
	}
	
	
	
	//push event with data
	//push invalid null event
	//push event with null data
	//assert data is delivered to the correct event handler
	//assert event is processed asynchronously
	//assert several events are delivered in the given MAX rate 
	//test retry logic
	//test error handling after retry fails
	@Test
	public void pushEventTest() throws InterruptedException {		
		TestEventHandler.onHandle = this::onEventHandle;
		TestEvent event = new TestEvent(ORG_ID, TestEventHandler.EXPECTED_DATA);
		
		integration.pushIntegrationEvent(event, this::assertEventComplete , this::onPushEventError);
		
		Duration pushDuration = Duration.between(event.getCreationTime(), LocalDateTime.now());
		System.out.println("Push Duration in Mills: " +  pushDuration.toMillis());
		
		assertTrue("event push is asynchronous ,it should be done fast, and must return before handling the event!"
				  , pushDuration.toMillis() < HANDLE_DELAY_MS);	
		
		Thread.sleep(HANDLE_DELAY_MS + 500);
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
	
	
	
	
	
	
	private void assertEventComplete(TestEvent event){
		assertEquals(TestEventHandler.EXPECTED_RESULT, event.getEventResult());
		
		Duration eventHandlingDuration = Duration.between(event.getCreationTime(), event.getResultRecievedTime());
		System.out.println("Handle Duration in Mills: " +  eventHandlingDuration.toMillis());
		
		assertTrue(eventHandlingDuration.toMillis() >= HANDLE_DELAY_MS);
	}
	
	
	
	private void onPushEventError(TestEvent event, Throwable t) {
		assertTrue("integration error happended!", false);
		t.printStackTrace();
	}
}
