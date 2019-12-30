import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.nasnav.test.integration.IntegrationApiTest;
import com.nasnav.test.integration.IntegrationServiceTest;
import com.nasnav.test.integration.msdynamics.MicrosoftDynamicsIntegrationTest;
import com.nasnav.test.integration.msdynamics.MicrosoftDynamicsIntegrationWebClientsTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	IntegrationServiceTest.class	
//	, MicrosoftDynamicsIntegrationTest.class
	, IntegrationApiTest.class	
//	, MicrosoftDynamicsIntegrationWebClientsTest.class
	})
public class IntegrationTestSuite {

}
