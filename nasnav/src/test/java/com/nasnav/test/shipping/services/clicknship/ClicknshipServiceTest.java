package com.nasnav.test.shipping.services.clicknship;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationShippingServiceRepository;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.services.clicknship.webclient.ClickNshipWebClient;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.nasnav.shipping.services.clicknship.ClickNShipShippingService.AWB_MIME;
import static com.nasnav.shipping.services.clicknship.ClickNShipShippingService.SERVICE_ID;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@DirtiesContext
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_10.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ClicknshipServiceTest {



    @Autowired
    private ShippingServiceFactory shippingServiceFactory;

    @Autowired
    private ClicknshipTestCommon mockService;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationShippingServiceRepository serviceParamRepo;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);


    private String trackingUrl = "https://clicknship.com.ng/GuestOperations/ShipmentTrackerGuest.aspx?WaybillNumber=";
    private String token = "nBYCv3UXLzxYrO77JTzXc76geUKXU5bqDRuB-JsCc3wMmkweTbR0ZjiIvsDmXA5XNX_yJKh6CEVS1Ag_xioGHZyzuDTDvnygnPj6MRQBusT8EYcJzTJvE2ry1aZs1fCLY_4zD4EjULfXfNVSBVvof5CXOqHlfrYjEA7eiYOTYOd0TZz3v120Gaqjpur6a4HmvMFzY6FYvGRt-ONyIaPoxUvB5HVyI1Q3YZKiXCsAz3G3WRNZC-oO8TO60PNBx8viyhrU5bDNl-yEKkeh9yIoRd2V3rZRtKymcEoj1xYnxjYWu3XBRD_jgVY_65h2Bb3AnSB7j_qSHoZF3i3siyqGUp4OFOs6IFIh5pgLcBz6nha-cBz5W7u-S8pkQlSXPN-0FBAD6-GwpSxztaXKc0BGhUMbgh7ghWrPzSztdiMCVps";
    private static final String SERVER_URL = "https://api.clicknship.com.ng";
    private ClickNshipWebClient client;

    private String server = "";
//    private String server = SERVER_URL;       //uncomment while commenting the above line to run tests against the actual staging server


    private String serviceParams;

    @Before
    public void init() throws Exception {
        if(isUsingMockServer()){
            server = mockService.initMockServer(mockServerRule);
            updateServiceParams();
        }else{
            client = new ClickNshipWebClient(SERVER_URL);
        }
    }



    private void updateServiceParams() {
        var serviceParams =
                serviceParamRepo.getByOrganization_IdAndServiceId(99001L, SERVICE_ID).get();
        var paramsJson = new JSONObject(serviceParams.getServiceParameters());
        paramsJson.put("SERVER_URL", server);
        serviceParams.setServiceParameters(paramsJson.toString());
        serviceParamRepo.save(serviceParams);
    }




    @Test
    public void testGetOffer() {
        var service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();
        var details = createShippingDetails(randomLong(), randomLong());
        var offer = service.createShippingOffer(details).block();
        var shipments = offer.getShipments();
        assertTrue(offer.isAvailable());
        assertEquals(2, shipments.size());
        assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
        assertEquals(now().plusDays(4).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());

        if(isUsingMockServer()){
            assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("3547.50")));
        }
    }



    @Test
    public void testGetOfferWithErrorFromServer() {
        var service =
                shippingServiceFactory
                        .getShippingService(SERVICE_ID, createServiceParams())
                        .get();

        var details = createShippingDetails(randomLong(), randomLong());
        setCityTheReturnsError(details.get(0));

        var offer = service.createShippingOffer(details).blockOptional();
        assertTrue(offer.isPresent());
        assertFalse(offer.get().isAvailable());
    }



    @Test
    public void testCreateDeliveryRequest() {
        var service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();
        //when running the tests against the staging server, the metaOrder-subOrder combination must be unique
        Long metaOrderId = isUsingMockServer() ? 11L: randomLong();
        Long subOrderId = isUsingMockServer() ? 22L: randomLong();
        var details = createShippingDetails(metaOrderId, subOrderId);
        var tracker = service.requestShipment(details).blockFirst();
        assertEquals(format("%d-%d", metaOrderId, subOrderId), tracker.getShipmentExternalId());
        if(isUsingMockServer()){
            assertEquals("SA00712362", tracker.getTracker());
        }
        assertFalse(tracker.getAirwayBillFile().isEmpty());
        assertEquals(AWB_MIME, tracker.getAirwayBillFileMime());
        assertTrue(tracker.getAirwayBillFileName().matches(".*_trk_SA00712362_order_.*_.*\\.pdf"));
    }



    private boolean isUsingMockServer() {
        return !server.equals(SERVER_URL);
    }


    @Test
    public void testGetOfferCityOutOfService() {
        var service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        var details = createShippingDetails(randomLong(), randomLong());
        setOutOfReachCity(details.get(0));

        var offer = service.createShippingOffer(details).blockOptional();
        assertTrue(offer.isPresent());
        assertFalse(offer.get().isAvailable());
    }



    @Test(expected = RuntimeBusinessException.class)
    public void createDeliveryUnsupportedCityTest() {
        var service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        var details = createShippingDetails(randomLong(), randomLong());
        setOutOfReachCity(details.get(0));

        service.requestShipment(details).collectList().block().get(0);
    }



    @Test
    public void testGetMinimumOffer() throws IOException {
        HttpEntity<?> request =  getHttpEntity("123");
        var response =
                template.exchange("/shipping/offers?customer_address=12300001", GET, request, String.class);

        assertEquals(OK, response.getStatusCode());

        var offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
        var shipments = offers.get(0).getShipments();

        sort(shipments, comparing(ShipmentDTO::getShippingFee));
        assertEquals(1, shipments.size());
        assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
        assertEquals(now().plusDays(4).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());
        if(isUsingMockServer()){
            assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("3547.5")));
        }
    }



    @Test
    public void testGetFailingOffer() throws IOException {
        HttpEntity<?> request =  getHttpEntity("123");
        var response =
                template.exchange("/shipping/offers?customer_address=12300003", GET, request, String.class);

        assertEquals(OK, response.getStatusCode());

        var offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
        assertFalse(offers.get(0).isAvailable());
    }



    private Long randomLong(){
        return (long)(Math.random()*Long.MAX_VALUE);
    }




    private void setOutOfReachCity(ShippingDetails details) {
        var farFarAwayAddr = new ShippingAddress();
        farFarAwayAddr.setAddressLine1("Frozen Oil st.");
        farFarAwayAddr.setArea(191919L);
        farFarAwayAddr.setBuildingNumber("777");
        farFarAwayAddr.setCity(99999L);
        farFarAwayAddr.setCountry(99999L);
        farFarAwayAddr.setId(12300004L);
        farFarAwayAddr.setName("Nasnav North Pole Branch");
        details.setDestination(farFarAwayAddr);
    }



    private void setCityTheReturnsError(ShippingDetails details) {
        var addr = new ShippingAddress();
        addr.setName("PETER ADEOGUN");
        addr.setAddressLine1("32 AJOSE ADEOGUN STREET, VICTORIA ISLAND, LAGOS");
        addr.setArea(11L);
        addr.setCity(1005L);
        details.setSource(addr);
    }





    private List<ShippingDetails> createShippingDetails(Long metaOrderId, Long subOrderId) {
        var receiver = new ShipmentReceiver();
        receiver.setFirstName("John");
        receiver.setLastName("Smith");
        receiver.setPhone("08076522536");
        receiver.setEmail("testemail@yahoo.com");

        var source1 = new ShippingAddress();
        source1.setName("PETER ADEOGUN");
        source1.setAddressLine1("32 AJOSE ADEOGUN STREET, VICTORIA ISLAND, LAGOS");
        source1.setArea(11L);
        source1.setCity(1001L);

        var source2 = new ShippingAddress();
        source2.setName("PETER ADEOGUN");
        source2.setAddressLine1("32 KOMOMBO STREET, VICTORIA ISLAND, LAGOS");
        source2.setArea(11L);
        source2.setCity(1001L);

        var dest = new ShippingAddress();
        dest.setName("BENSON ADEWALE");
        dest.setAddressLine1("23 Ikorodu Road, Maryland, Lagos");
        dest.setArea(22L);
        dest.setCity(1002L);

        var item = new ShipmentItems();
        item.setWeight(new BigDecimal("1.5"));
        item.setName("HAND BAG");
        item.setQuantity(5);
        item.setPrice(new BigDecimal(9000));
        item.setSpecs("Color : BLUE, Size : 23");
        item.setStockId(310001L);

        var details1 = new ShippingDetails();
        details1.setReceiver(receiver);
        details1.setMetaOrderId(metaOrderId);
        details1.setSubOrderId(subOrderId);
        details1.setSource(source1);
        details1.setDestination(dest);
        details1.setItems(singletonList(item));

        var details2 = new ShippingDetails();
        details2.setReceiver(receiver);
        details2.setMetaOrderId(metaOrderId);
        details2.setSubOrderId(subOrderId);
        details2.setSource(source2);
        details2.setDestination(dest);
        details2.setItems(singletonList(item));
        return asList(details1, details2);
    }



    private List<ServiceParameter> createServiceParams() {
        return asList(new ServiceParameter("SERVER_URL", server)
                , new ServiceParameter("USER_NAME", "cnsdemoapiacct")
                , new ServiceParameter("PASSWORD", "ClickNShip$12345")
                , new ServiceParameter("GRANT_TYPE", "password")
                , new ServiceParameter("TRACKING_URL", trackingUrl));
    }


    //@Test
    public void authenticate() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            assertEquals(200, res.rawStatusCode());
        };
        client.authenticateUser("cnsdemoapiacct", "ClickNShip$12345", "password")
                .subscribe(c);
        Thread.sleep(1000);
    }



    ///@Test
    public void getCities() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            assertEquals(200, res.rawStatusCode());
        };
        client.getCities(token)
                .subscribe(c);
        Thread.sleep(1000);
    }



    //@Test
    public void getStates() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            assertEquals(200, res.rawStatusCode());
        };
        client.getStates(token)
                .subscribe(c);
        Thread.sleep(1000);
    }
}
