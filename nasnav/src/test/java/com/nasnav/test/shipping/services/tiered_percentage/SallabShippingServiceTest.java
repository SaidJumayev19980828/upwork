package com.nasnav.test.shipping.services.tiered_percentage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.nasnav.service.ShippingManagementService;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.nasnav.shipping.services.SallabShippingService.*;
import static com.nasnav.test.commons.TestCommons.*;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_8.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class SallabShippingServiceTest extends AbstractTestWithTempBaseDir {

    public static final String ETA_FROM = "1";
    public static final String ETA_TO = "1";
    @Autowired
    private ShippingServiceFactory shippingServiceFactory;

    @Autowired
    private ShippingManagementService shippingMgr;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper objectMapper;


    @Value("classpath:/files/sallab_shipping_service_tiers.json")
    private Resource parametersJson;


    @Test
    public void testSetParameters() throws IOException {
        String params = readResource(parametersJson);
        OrganizationShippingServiceEntity orgShippingParams =
                new OrganizationShippingServiceEntity();
        orgShippingParams.setServiceId(SERVICE_ID);
        orgShippingParams.setServiceParameters(params);


        Optional<ShippingService> service = shippingMgr.getShippingService(orgShippingParams);
        assertTrue(service.isPresent());
    }




    @Test
    public void testGetMinimumOffer() throws IOException {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response =
                template.exchange("/shipping/offers?customer_address=12300001", GET, request, String.class);

        assertEquals(OK, response.getStatusCode());

        List<ShippingOfferDTO> offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
        List<ShipmentDTO> shipments = offers.get(0).getShipments();

        sort(shipments, comparing(ShipmentDTO::getShippingFee));
        assertEquals(3, shipments.size());
        assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("16.66")));
        assertEquals(0, shipments.get(1).getShippingFee().compareTo(new BigDecimal("16.66")));
        assertEquals(0, shipments.get(2).getShippingFee().compareTo(new BigDecimal("16.68")));
        assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
        assertEquals(now().plusDays(3).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());
    }




    @Test
    public void testGetOfferOnTier5Prices() {
        ShippingService service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetailsWithTier5Prices();

        ShippingOffer offer = service.createShippingOffer(details).block();
        List<Shipment> shipments = offer.getShipments( );
        sort(shipments, comparing(Shipment::getShippingFee));
        assertEquals(2, shipments.size());
        assertEquals(0, shipments.get(0).getShippingFee().compareTo(ZERO));
        assertEquals(0 , shipments.get(1).getShippingFee().compareTo(ZERO));
        assertEquals(now().plusDays(Integer.parseInt(ETA_FROM)).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
        assertEquals(now().plusDays(Integer.parseInt(ETA_TO)).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());
    }



    @Test
    public void testGetOffer() {
        ShippingService service = shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();

        ShippingOffer offer = service.createShippingOffer(details).block();
        List<Shipment> shipments = offer.getShipments( );
        sort(shipments, comparing(Shipment::getShippingFee));
        assertEquals(2, shipments.size());
        assertEquals(0 , shipments.get(0).getShippingFee().compareTo(new BigDecimal("41")));
        assertEquals(0, shipments.get(1).getShippingFee().compareTo(new BigDecimal("50")));
        assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
        assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());
    }




    @Test
    public void testGetOfferCityOutOfService() {
        ShippingService service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();
        setOutOfReachCity(details.get(0));

        Mono<ShippingOffer> offer = service.createShippingOffer(details);
        assertFalse(offer.blockOptional().isPresent());
    }




    @Test
    public void createDeliveryTest() {
        ShippingService service = 
        		shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();

        ShipmentTracker tracker = service.requestShipment(details).collectList().block().get(0);

        assertNull(tracker.getShipmentExternalId());
        assertNull(tracker.getTracker());
        assertNull(tracker.getAirwayBillFile());
    }






    @Test(expected = RuntimeBusinessException.class)
    public void createDeliveryUnsupportedCityTest() {
        ShippingService service =
                shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();
        setOutOfReachCity(details.get(0));

        service.requestShipment(details).collectList().block().get(0);
    }





    private List<ServiceParameter> createServiceParams() {
    	String tiersJson =
    			json()
    			.put("tiers", 
    					jsonArray()
    						.put(createTierJson(0, 10000, 1d, 0))
    						.put(createTierJson(10000, 25001, 3d, 0))
    						.put(createTierJson(25001, 50001, 1.5d, 0))
    						.put(createTierJson(50001, 100001, 1d, 0))
    						.put(createTierJson(100001, Integer.MAX_VALUE, 1d, 3)))
                .toString();
    	JSONArray supportedCitiesJson = jsonArray().put(1L).put(3L); 
    			
        return asList(new ServiceParameter(TIERS, tiersJson)
        		     , new ServiceParameter(SUPPORTED_CITIES, supportedCitiesJson.toString())
                    , new ServiceParameter(MIN_SHIPPING_FEE, "50")
                    , new ServiceParameter(ETA_DAYS_MIN, ETA_FROM)
                    , new ServiceParameter(ETA_DAYS_MAX, ETA_TO));
    }




	private JSONObject createTierJson(Integer startInclusive, Integer endExclusive, Double percentage, Integer maxFreeShipments) {
		return json()
				.put("start_inclusive", startInclusive)
				.put("end_exclusive", endExclusive)
				.put("percentage", percentage)
				.put("max_free_shipments", maxFreeShipments);
	}




    private void setOutOfReachCity(ShippingDetails details) {
        ShippingAddress farFarAwayAddr = new ShippingAddress();
        farFarAwayAddr.setAddressLine1("Frozen Oil st.");
        farFarAwayAddr.setArea(191919L);
        farFarAwayAddr.setBuildingNumber("777");
        farFarAwayAddr.setCity(99999L);
        farFarAwayAddr.setCountry(99999L);
        farFarAwayAddr.setId(12300004L);
        farFarAwayAddr.setName("Nasnav North Pole Branch");
        details.setDestination(farFarAwayAddr);
    }






    private List<ShippingDetails> createShippingsDetails() {
        ShippingAddress customerAddr = new ShippingAddress();
        customerAddr.setAddressLine1("Mama st.");
        customerAddr.setArea(181818L);
        customerAddr.setBuildingNumber("555");
        customerAddr.setCity(3L);
        customerAddr.setCountry(1L);
        customerAddr.setFlatNumber("5A");
        customerAddr.setId(12300001L);
        customerAddr.setName("Hamada Ezzo");


        ShippingAddress shopAddr1 = new ShippingAddress();
        shopAddr1.setAddressLine1("Food court st.");
        shopAddr1.setArea(191919L);
        shopAddr1.setBuildingNumber("777");
        shopAddr1.setCity(1L);
        shopAddr1.setCountry(1L);
        shopAddr1.setId(12300002L);
        shopAddr1.setName("7rnksh Nasnav");


        ShippingAddress shopAddr2 = new ShippingAddress();
        shopAddr2.setAddressLine1("Food court st.");
        shopAddr2.setArea(171717L);
        shopAddr2.setBuildingNumber("888");
        shopAddr2.setCity(1L);
        shopAddr2.setCountry(1L);
        shopAddr2.setId(12300003L);
        shopAddr2.setName("Freska Nasnav");

        ShipmentReceiver receiver = new ShipmentReceiver();
        receiver.setFirstName("Sponge");
        receiver.setLastName("Bob");
        receiver.setPhone("01000000000");

        List<ShipmentItems> itemsOfShop1 = asList( createShipmentItems(601L, new BigDecimal("2500"), 2));
        List<ShipmentItems> itemsOfShop2 = asList( createShipmentItems(603L, new BigDecimal("4100"), 1));

        ShippingDetails shippingDetails1 = new ShippingDetails();
        shippingDetails1.setDestination(customerAddr);
        shippingDetails1.setSource(shopAddr1);
        shippingDetails1.setItems(itemsOfShop1);
        shippingDetails1.setReceiver(receiver);
        shippingDetails1.setShopId(502L);

        ShippingDetails shippingDetails2 = new ShippingDetails();
        shippingDetails2.setDestination(customerAddr);
        shippingDetails2.setSource(shopAddr2);
        shippingDetails2.setItems(itemsOfShop2);
        shippingDetails2.setReceiver(receiver);
        shippingDetails2.setShopId(501L);


        List<ShippingDetails> details = asList(shippingDetails1, shippingDetails2);
        return details;
    }
    
    
    
    private ShipmentItems createShipmentItems(Long stockId, BigDecimal price, Integer quantity) {
    	ShipmentItems item = new ShipmentItems();
    	item.setPrice(price);
    	item.setStockId(stockId);
    	item.setQuantity(quantity);
    	return item;
    }





    private List<ShippingDetails> createShippingsDetailsWithTier5Prices() {
        ShippingAddress customerAddr = new ShippingAddress();
        customerAddr.setAddressLine1("Mama st.");
        customerAddr.setArea(181818L);
        customerAddr.setBuildingNumber("555");
        customerAddr.setCity(3L);
        customerAddr.setCountry(1L);
        customerAddr.setFlatNumber("5A");
        customerAddr.setId(12300001L);
        customerAddr.setName("Hamada Ezzo");


        ShippingAddress shopAddr1 = new ShippingAddress();
        shopAddr1.setAddressLine1("Food court st.");
        shopAddr1.setArea(191919L);
        shopAddr1.setBuildingNumber("777");
        shopAddr1.setCity(1L);
        shopAddr1.setCountry(1L);
        shopAddr1.setId(12300002L);
        shopAddr1.setName("7rnksh Nasnav");


        ShippingAddress shopAddr2 = new ShippingAddress();
        shopAddr2.setAddressLine1("Food court st.");
        shopAddr2.setArea(171717L);
        shopAddr2.setBuildingNumber("888");
        shopAddr2.setCity(1L);
        shopAddr2.setCountry(1L);
        shopAddr2.setId(12300003L);
        shopAddr2.setName("Freska Nasnav");

        ShipmentReceiver receiver = new ShipmentReceiver();
        receiver.setFirstName("Sponge");
        receiver.setLastName("Bob");
        receiver.setPhone("01000000000");

        List<ShipmentItems> itemsOfShop1 = asList( createShipmentItems(601L, new BigDecimal("51000"), 2));
        List<ShipmentItems> itemsOfShop2 = asList( createShipmentItems(603L, new BigDecimal("110000"), 1));

        ShippingDetails shippingDetails1 = new ShippingDetails();
        shippingDetails1.setDestination(customerAddr);
        shippingDetails1.setSource(shopAddr1);
        shippingDetails1.setItems(itemsOfShop1);
        shippingDetails1.setReceiver(receiver);
        shippingDetails1.setShopId(502L);

        ShippingDetails shippingDetails2 = new ShippingDetails();
        shippingDetails2.setDestination(customerAddr);
        shippingDetails2.setSource(shopAddr2);
        shippingDetails2.setItems(itemsOfShop2);
        shippingDetails2.setReceiver(receiver);
        shippingDetails2.setShopId(501L);


        List<ShippingDetails> details = asList(shippingDetails1, shippingDetails2);
        return details;
    }

}
