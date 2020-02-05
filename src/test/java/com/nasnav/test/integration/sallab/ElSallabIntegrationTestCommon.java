package com.nasnav.test.integration.sallab;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.file.Files;

import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.net.MediaType;


@Component
public class ElSallabIntegrationTestCommon {
	

	public  final String mockServerUrl = "http://127.0.0.1";
	
	
	
	@Value("classpath:/json/el_sallab_integration_test/auth_api_response.json")
	private Resource authResponseJson;
    
    
    @Value("classpath:/json/el_sallab_integration_test/get_products_response.json")
	private Resource productsResponseJson;
    
    
    
    
    

	public  String initElSallabMockServer(MockServerRule mockServerRule) throws Exception {		
		prepareMockRequests(mockServerRule);
		return mockServerUrl + ":"+ mockServerRule.getPort();
	}
	
	
	
	
	
	
	
	private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
		mockGetProductsRequest(mockServerRule);
		mockGetAuthRequest(mockServerRule);//TODO: auth is on different domain, it needs separate mock server- or not
	}
	
	
	

	
	
	private  void mockGetProductsRequest(MockServerRule mockServerRule) throws IOException {
		String productsResponse = readJsonFile(productsResponseJson);
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/services/data/v44.0/query"))
			.respond(
					response().withBody(productsResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	private  void mockGetAuthRequest(MockServerRule mockServerRule) throws IOException {
		String authResponse = readJsonFile(authResponseJson);
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/services/oauth2/token"))
			.respond(
					response().withBody(authResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}







	private String readJsonFile(Resource resource) throws IOException {
		String productsResponse = new String( Files.readAllBytes(resource.getFile().toPath()) );
		return productsResponse;
	}
	
}
