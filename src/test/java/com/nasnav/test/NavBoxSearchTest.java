package com.nasnav.test;

import com.nasnav.NavBox;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.test.commons.TestCommons.readResourceFileAsString;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/ExtraAttributes_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
public class NavBoxSearchTest {
    public  final String mockServerUrl = "http://127.0.0.1";
    private String mockServerFullUrl;

    @Value("classpath:/json/elastic_search/search_response.json")
    private Resource searchResponseJson;


    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);





    @Before
    public  void initElasticSearchMockServer() throws Exception {
        prepareMockRequests(mockServerRule);
        mockServerFullUrl =  mockServerUrl + ":"+ mockServerRule.getPort();
    }



    private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
        mockSearchRequest(mockServerRule);
    }



    private  void mockSearchRequest(MockServerRule mockServerRule) throws IOException {
        String productsResponse = readResourceFileAsString(searchResponseJson);
        mockServerRule.getClient()
                .when(
                        request().withMethod("GET")
                                .withPath("/_search"))
                .respond(
                        response().withBody(productsResponse, JSON_UTF_8)
                                .withStatusCode(200))
        ;
    }
}
