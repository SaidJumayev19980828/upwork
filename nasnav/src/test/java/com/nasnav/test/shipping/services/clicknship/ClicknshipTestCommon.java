package com.nasnav.test.shipping.services.clicknship;

import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.test.commons.TestCommons.readResource;
import static org.mockserver.matchers.MatchType.ONLY_MATCHING_FIELDS;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

@Component
public class ClicknshipTestCommon {
    public final String mockServerUrl = "http://127.0.0.1";

    @Value("classpath:/json/shipping/service/clicknship/create_delivery_response.json")
    private Resource createDeliveryResponseJson;

    public String initMockServer(MockServerRule mockServerRule) throws Exception {
        prepareMockRequests(mockServerRule);
        return mockServerUrl + ":" + mockServerRule.getPort();
    }



    private void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
        mockCreateDeliveryReturningErrorRequest(mockServerRule);
        mockCreateDeliveryReturningErrorRequest2(mockServerRule);
        mockAuthenticationRequest(mockServerRule);
        mockCreateOfferRequest(mockServerRule);
        mockCreateDeliveryRequest(mockServerRule);
        mockCreateAirwayBillRequest(mockServerRule);
    }

    private void mockAuthenticationRequest(MockServerRule mockServerRule) {
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withPath("/Token"))
                .respond(
                        response().withBody("{ \"access_token\":\"\"} ", JSON_UTF_8)
                                .withStatusCode(200))
        ;
    }

    private void mockCreateOfferRequest(MockServerRule mockServerRule) throws IOException {
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withHeader("Authorization", ".+")
                                .withPath("/clicknship/Operations/DeliveryFee"))
                .respond(
                        response().withBody("[  {\"DeliveryFee\": 3300.00,\"VatAmount\": 247.50000,\"TotalAmount\": 3547.50000}]", JSON_UTF_8)
                                .withStatusCode(200))
        ;
    }

    private void mockCreateDeliveryRequest(MockServerRule mockServerRule) throws IOException {
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withHeader("Authorization", ".+")
                                .withPath("/clicknship/Operations/PickupRequest"))
                .respond(
                        response().withBody( readResource(createDeliveryResponseJson), JSON_UTF_8)
                                .withStatusCode(200));

    }



    private void mockCreateDeliveryReturningErrorRequest(MockServerRule mockServerRule) throws IOException {
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withHeader("Authorization", ".+")
                                .withBody(
                                        json("{'Origin': 'ADO EKITI'}"
                                            , ONLY_MATCHING_FIELDS))
                                .withPath("/clicknship/Operations/DeliveryFee"))
                .respond(response().withStatusCode(500));
    }



    private void mockCreateDeliveryReturningErrorRequest2(MockServerRule mockServerRule) throws IOException {
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withHeader("Authorization", ".+")
                                .withBody(
                                        json("{'Destination': 'ADO EKITI'}"
                                                , ONLY_MATCHING_FIELDS))
                                .withPath("/clicknship/Operations/DeliveryFee"))
                .respond(response().withStatusCode(500));
    }



    private void mockCreateAirwayBillRequest(MockServerRule mockServerRule) throws IOException {
        mockServerRule.getClient()
                .when(
                        request().withMethod("GET")
                                .withHeader("Authorization", ".+")
                                .withPath("/clicknship/Operations/PrintWaybill"))
                .respond(
                        response().withBody( readResource(createDeliveryResponseJson), JSON_UTF_8)
                                .withStatusCode(200));

    }
}
