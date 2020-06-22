package com.nasnav.test.shipping.services.bosta;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.file.Files;

import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


@Component
public class BostaLevisTestCommon {
	

	public  final String mockServerUrl = "http://127.0.0.1";
	
	
	
	@Value("classpath:/json/shipping/service/bosta/create_delivery_response.json")
	private Resource createDeliveryResponseJson;
    
    
    
    

	public  String initBostaMockServer(MockServerRule mockServerRule) throws Exception {		
		prepareMockRequests(mockServerRule);
		return mockServerUrl + ":"+ mockServerRule.getPort();
	}
	
	
	
	
	
	
	
	private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
		mockCreateDeliveryRequest(mockServerRule);
		mockCreateBillRequest(mockServerRule);
	}
	
	
	

	
	
	private void mockCreateDeliveryRequest(MockServerRule mockServerRule) throws IOException {
		 mockServerRule.getClient()
			.when(
				request().withMethod("POST")
						.withHeader("Authorization", ".+")
						.withPath("/deliveries"))
			.respond(
					response().withBody(readJsonFile(createDeliveryResponseJson), JSON_UTF_8) 
							  .withStatusCode(201))
				;
	}
	
	
	
	
	
	private void mockCreateBillRequest(MockServerRule mockServerRule) throws IOException {
		 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withHeader("Authorization", ".+")
						.withPath("/deliveries/awb")
						.withQueryStringParameter(new Parameter("id", ".+")))
			.respond(
					response().withBody("{\"data\":\"NOT EMPTY AWB\"}", JSON_UTF_8) 
							  .withStatusCode(201))
				;
	}


	



	private String readJsonFile(Resource resource) throws IOException {
		String productsResponse = new String( Files.readAllBytes(resource.getFile().toPath()) );
		return productsResponse;
	}
}
