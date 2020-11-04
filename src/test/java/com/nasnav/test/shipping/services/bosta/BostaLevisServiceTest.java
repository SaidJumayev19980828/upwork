package com.nasnav.test.shipping.services.bosta;

import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.ALEXANDRIA_PRICE;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.AUTH_TOKEN_PARAM;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.BUSINESS_ID_PARAM;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.CAIRO_PRICE;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.DELTA_CANAL_PRICE;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.SERVER_URL;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.SERVICE_ID;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.UPPER_EGYPT_PRICE;
import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.WEBHOOK_URL;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.Shipment;
import com.nasnav.shipping.model.ShipmentItems;
import com.nasnav.shipping.model.ShipmentReceiver;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingAddress;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingOffer;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@DirtiesContext
public class BostaLevisServiceTest {
	
	private final String token = "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357";
    private String id = "yM1ngytZ0";
    private String server = "";
    private String webhook = "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99001";
    
    
    @Rule
	 public MockServerRule mockServerRule = new MockServerRule(this);

    
    @Autowired
    private BostaLevisTestCommon mockService;
    
    
    @Autowired
    private ShippingServiceFactory shippingServiceFactory;
    
    
    @Before
    public void init() throws Exception {
    	server = mockService.initBostaMockServer(mockServerRule);
    }
    
    
	
	@Test
	public void testGetOffer() {
		ShippingService service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();
		
		List<ShippingDetails> details = createShippingsDetails();
		
		ShippingOffer offer = service.createShippingOffer(details).block();
		List<Shipment> shipments = offer.getShipments();
		assertEquals(2, shipments.size());
		assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("30")));
		assertEquals(0 , shipments.get(1).getShippingFee().compareTo(ZERO));
		assertEquals(now().plusDays(1) , shipments.get(0).getEta().getFrom());
		assertEquals(now().plusDays(2) , shipments.get(0).getEta().getTo());
	}

	
	
	
	@Test
	public void testGetOfferCityOutOfService() {
		ShippingService service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();
		
		List<ShippingDetails> details = createShippingsDetails();
		setOutOfReachCity(details.get(0));
		
		Mono<ShippingOffer> offer = service.createShippingOffer(details);
		assertFalse(offer.blockOptional().isPresent());
	}
	
	
	
	
	@Test
	public void createDeliveryTest() {
		ShippingService service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();
		
		List<ShippingDetails> details = createShippingsDetails();
		
		ShipmentTracker tracker = service.requestShipment(details).collectList().block().get(0);
		
		assertEquals("8xY8LmHtJ", tracker.getShipmentExternalId());
		assertEquals("6272455", tracker.getTracker());
		assertNotNull(tracker.getAirwayBillFile());
		assertFalse(tracker.getAirwayBillFile().isEmpty());
	}

	
	
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	public void createDeliveryUnsupportedCityTest() {
		ShippingService service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();
		
		List<ShippingDetails> details = createShippingsDetails();
		setOutOfReachCity(details.get(0));
		
		service.requestShipment(details).collectList().block().get(0);
	}
	
	
	
	

	private List<ServiceParameter> createServiceParams() {
		return asList(new ServiceParameter(AUTH_TOKEN_PARAM,token)
				, new ServiceParameter(BUSINESS_ID_PARAM, id)
				, new ServiceParameter(SERVER_URL, server)
				, new ServiceParameter(WEBHOOK_URL, webhook)
				, new ServiceParameter(CAIRO_PRICE, "25")
				, new ServiceParameter(ALEXANDRIA_PRICE, "30")
				, new ServiceParameter(DELTA_CANAL_PRICE, "30")
				, new ServiceParameter(UPPER_EGYPT_PRICE, "45"));
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
		
		List<ShipmentItems> itemsOfShop1 = asList( new ShipmentItems(601L));
		List<ShipmentItems> itemsOfShop2 = asList( new ShipmentItems(603L));
		
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
