package com.nasnav.test.integration.sallab;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.test.commons.TestCommons.readResourceFileAsString;
import static com.nasnav.test.commons.TestCommons.readResourceFileBytes;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.file.Files;

import com.nasnav.test.commons.TestCommons;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


@Component
public class ElSallabIntegrationTestCommon {
	

	public  final String mockServerUrl = "http://127.0.0.1";
	
	
	
	@Value("classpath:/json/el_sallab_integration_test/auth_api_response.json")
	private Resource authResponseJson;
    
    
    @Value("classpath:/json/el_sallab_integration_test/get_products_response.json")
	private Resource productsResponseJson;
    
    
    
    @Value("classpath:/json/el_sallab_integration_test/get_stock_response.json")
    private Resource stockResponseJson;
    
    
    
    @Value("classpath:/json/el_sallab_integration_test/get_price_response.json")
    private Resource priceResponseJson;
    
    
    @Value("classpath:/json/el_sallab_integration_test/get_next_products_response.json")
    private Resource nextProductsResponseJson;
    
    
    @Value("classpath:/static/static/kitako/test_photo_1.png")   
    private Resource img1;
    

	public  String initElSallabMockServer(MockServerRule mockServerRule) throws Exception {		
		prepareMockRequests(mockServerRule);
		return mockServerUrl + ":"+ mockServerRule.getPort();
	}
	
	
	
	
	
	
	
	private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
		mockGetProductsRequest(mockServerRule);
		mockGetProductsNextRecordRequest(mockServerRule);
		mockGetAuthRequest(mockServerRule);
		mockGetPriceRequest(mockServerRule);
		mockGetItemStockRequest(mockServerRule);
		mockGetImgRequest(mockServerRule);
	}
	
	
	

	
	
	private void mockGetImgRequest(MockServerRule mockServerRule) throws IOException {
	    byte[] img = readImgFile(img1);
		 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/services/data/v36.0/sobjects/Attachment/.+/Body"))
			.respond(
					response().withBody(img) 
							  .withStatusCode(200))
				;
	}







	private  void mockGetProductsRequest(MockServerRule mockServerRule) throws IOException {
		String productsResponse = readJsonFile(productsResponseJson);
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/services/data/v44.0/query"))
			.respond(
					response().withBody(productsResponse, JSON_UTF_8)
							  .withStatusCode(200))
				;
	}
	
	
	
	
	
	
	private  void mockGetProductsNextRecordRequest(MockServerRule mockServerRule) throws IOException {
		String nextProductsResponse = readJsonFile(nextProductsResponseJson);
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/services/data/v44.0/query/.+"))
			.respond(
					response().withBody(nextProductsResponse, JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	
	
	private  void mockGetAuthRequest(MockServerRule mockServerRule) throws IOException {
		String authResponse = readJsonFile(authResponseJson);
    	 mockServerRule.getClient()
			.when(
				request()
					.withMethod("POST")
					.withPath("/services/oauth2/token")
				)
			.respond(
					response().withBody(authResponse, JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}


	
	
	
	
	private  void mockGetPriceRequest(MockServerRule mockServerRule) throws IOException {
		String priceResponse = readJsonFile(priceResponseJson);
    	 mockServerRule.getClient()
			.when(
				request()
					.withMethod("GET")
					.withPath("/ElSallab.Webservice/SallabService.svc/getItemPriceBreakdown")
				)
			.respond(
					response().withBody(priceResponse, JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	
	
	private  void mockGetItemStockRequest(MockServerRule mockServerRule) throws IOException {
		String stockResponse = readJsonFile(stockResponseJson);
    	 mockServerRule.getClient()
			.when(
				request()
					.withMethod("GET")
					.withPath("/ElSallab.Webservice/SallabService.svc/getItemStockBalance")
				)
			.respond(
					response().withBody(stockResponse, JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	

	



	private String readJsonFile(Resource resource) throws IOException {
		return readResourceFileAsString(resource);
	}
	
	
	

	private byte[] readImgFile(Resource resource) throws IOException {
		return readResourceFileBytes(resource);
	}
	
	
}
