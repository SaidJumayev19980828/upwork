package com.nasnav.test.bulkimport.img;

import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@Component
public class ImageBulkUrlUploadTestCommon {
	

	public  final String mockServerUrl = "http://127.0.0.1";
	
	
    
	@Value("classpath:/static/static/kitako/test_photo_1.png")   
    private Resource img1;
    
    
	@Value("classpath:/static/static/test_photo_2.png")
    private Resource img2;
    

	public  String initImgsMockServer(MockServerRule mockServerRule) throws Exception {
		prepareMockRequests(mockServerRule);
		return mockServerUrl + ":"+ mockServerRule.getPort();
	}
	
	
	
	
	
	
	
	private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
		mockGetImage1Request(mockServerRule);
		mockGetImage2Request(mockServerRule);
		mockGetImageAndFail(mockServerRule);
	}
	
	
	
	
	private void mockGetImageAndFail(MockServerRule mockServerRule) throws IOException {
   	 	mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/static/test_photo_failed.png"))
			.respond(
					response().withStatusCode(404))
				;
	}
	
	
	

	
	
	private void mockGetImage2Request(MockServerRule mockServerRule) throws IOException {
		byte[] img = readImgFile(img2);
   	 	mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/static/test_photo_2.png"))
			.respond(
					response().withBody(img) 
							  .withStatusCode(200))
				;
	}







	private  void mockGetImage1Request(MockServerRule mockServerRule) throws IOException {
		byte[] img = readImgFile(img1);
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/static/kitako/test_photo_1.png"))
			.respond(
					response().withBody(img) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	

	



	private byte[] readImgFile(Resource resource) throws IOException {
		return Files.readAllBytes(resource.getFile().toPath());
	}
	
}
