package com.nasnav.test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.nasnav.test.integration.IntegrationApiTest;
import com.nasnav.test.integration.IntegrationDictApiTest;
import com.nasnav.test.integration.IntegrationErrorsApiTest;
import com.nasnav.test.integration.IntegrationServiceTest;
import com.nasnav.test.integration.msdynamics.MicrosoftDynamicsIntegrationTest;
import com.nasnav.test.integration.msdynamics.MicrosoftDynamicsIntegrationWebClientsTest;
import com.nasnav.test.integration.sallab.ElSallabIntegrationTest;

//@RunWith(Suite.class)
//@SuiteClasses({ 
//	 IntegrationApiTest.class	
//	, IntegrationErrorsApiTest.class
//	, IntegrationDictApiTest.class
//	, MicrosoftDynamicsIntegrationTest.class
//	, IntegrationServiceTest.class
//	, MicrosoftDynamicsIntegrationWebClientsTest.class
//	, ElSallabIntegrationTest.class
//	})
public class IntegrationTestSuite {

}
