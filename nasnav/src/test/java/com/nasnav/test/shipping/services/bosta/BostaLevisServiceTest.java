package com.nasnav.test.shipping.services.bosta;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static com.nasnav.shipping.services.bosta.BostaLevisShippingService.*;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
// @DirtiesContext
public class BostaLevisServiceTest extends AbstractTestWithTempBaseDir {

	private final String trackingUrl = "https://bosta.co/tracking-shipment/?lang=en&track_num=";
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
		var service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();

		var details = createShippingsDetails();

		var offer = service.createShippingOffer(details).block();
		var shipments = offer.getShipments();
		assertEquals(2, shipments.size());
		assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("30")));
		assertEquals(0 , shipments.get(1).getShippingFee().compareTo(ZERO));
		assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
		assertEquals(now().plusDays(2).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());
	}

	
	
	
	@Test
	public void testGetOfferCityOutOfService() {
		var service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();

		var details = createShippingsDetails();
		setOutOfReachCity(details.get(0));

		var offer = service.createShippingOffer(details);
		assertFalse(offer.blockOptional().isPresent());
	}
	
	
	
	
	@Test
	public void createDeliveryTest() {
		var service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();

		var details = createShippingsDetails();

		var tracker = service.requestShipment(details).collectList().block().get(0);
		
		assertEquals("8xY8LmHtJ", tracker.getShipmentExternalId());
		assertEquals("6272455", tracker.getTracker());
		assertNotNull(tracker.getAirwayBillFile());
		assertEquals(AWB_MIME, tracker.getAirwayBillFileMime());
		assertTrue(tracker.getAirwayBillFileName().matches(".*_trk_6272455_order_.*_.*\\.pdf"));
		assertFalse(tracker.getAirwayBillFile().isEmpty());
	}

	
	
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	public void createDeliveryUnsupportedCityTest() {
		var service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();

		var details = createShippingsDetails();
		setOutOfReachCity(details.get(0));
		
		service.requestShipment(details).collectList().block().get(0);
	}
	
	
	
	

	private List<ServiceParameter> createServiceParams() {
		return asList(new ServiceParameter(TRACKING_URL,trackingUrl)
				, new ServiceParameter(AUTH_TOKEN_PARAM,token)
				, new ServiceParameter(BUSINESS_ID_PARAM, id)
				, new ServiceParameter(SERVER_URL, server)
				, new ServiceParameter(WEBHOOK_URL, webhook)
				, new ServiceParameter(CAIRO_PRICE, "25")
				, new ServiceParameter(ALEXANDRIA_PRICE, "30")
				, new ServiceParameter(DELTA_CANAL_PRICE, "30")
				, new ServiceParameter(UPPER_EGYPT_PRICE, "45"));
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
	
	
	
	
	

	private List<ShippingDetails> createShippingsDetails() {
		var customerAddr = new ShippingAddress();
		customerAddr.setId(123L);
		customerAddr.setAddressLine1("Mama st.");
		customerAddr.setArea(181818L);
		customerAddr.setBuildingNumber("555");
		customerAddr.setCity(3L);
		customerAddr.setCountry(1L);
		customerAddr.setFlatNumber("5A");
		customerAddr.setId(12300001L);
		customerAddr.setName("Hamada Ezzo");


		var shopAddr1 = new ShippingAddress();
		shopAddr1.setAddressLine1("Food court st.");
		shopAddr1.setArea(191919L);
		shopAddr1.setBuildingNumber("777");
		shopAddr1.setCity(1L);
		shopAddr1.setCountry(1L);
		shopAddr1.setId(12300002L);
		shopAddr1.setName("7rnksh Nasnav");


		var shopAddr2 = new ShippingAddress();
		shopAddr2.setAddressLine1("Food court st.");
		shopAddr2.setArea(171717L);
		shopAddr2.setBuildingNumber("888");
		shopAddr2.setCity(1L);
		shopAddr2.setCountry(1L);
		shopAddr2.setId(12300003L);
		shopAddr2.setName("Freska Nasnav");

		var receiver = new ShipmentReceiver();
		receiver.setFirstName("Sponge");
		receiver.setLastName("Bob");
        receiver.setPhone("01000000000");

		var itemsOfShop1 = asList( new ShipmentItems(601L));
		var itemsOfShop2 = asList( new ShipmentItems(603L));

		var shippingDetails1 = new ShippingDetails();
		shippingDetails1.setDestination(customerAddr);
		shippingDetails1.setSource(shopAddr1);
		shippingDetails1.setItems(itemsOfShop1);
		shippingDetails1.setReceiver(receiver);
		shippingDetails1.setShopId(502L);

		var shippingDetails2 = new ShippingDetails();
		shippingDetails2.setDestination(customerAddr);
		shippingDetails2.setSource(shopAddr2);
		shippingDetails2.setItems(itemsOfShop2);
		shippingDetails2.setReceiver(receiver);
		shippingDetails2.setShopId(501L);


		var details = asList(shippingDetails1, shippingDetails2);
		return details;
	}
}
