package com.nasnav.test.shipping.services.pickup_point;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingAdditionalDataDTO;
import com.nasnav.dto.request.shipping.ShippingEtaDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_MIME;
import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_NAME;
import static com.nasnav.shipping.services.PickupPointsWithInternalLogistics.*;
import static com.nasnav.shipping.services.SallabShippingService.ETA_DAYS_MAX;
import static com.nasnav.shipping.services.SallabShippingService.ETA_DAYS_MIN;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_7.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PickupPointServiceTest {

	private static final Integer ETA_FROM = 1;
	private static final Integer ETA_TO = 4;
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
    private ShippingServiceFactory shippingServiceFactory;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	
	//create offers, always return stocks in warehouse
	@Test
	public void createOfferTest() throws Exception{
		Long customerAddress = 12300001L;
		HttpEntity<?> request =  getHttpEntity("123");
		var response =
        		template.exchange("/shipping/offers?customer_address="+customerAddress, GET, request, String.class);

		var body = ofNullable(response.getBody()).orElse("[]");
		var offers = mapper.readValue(body, new TypeReference<List<ShippingOfferDTO>>(){});
		var pickupPoints = getOfferedShops(offers);
		var shops = getOfferedStockShops(offers);
        
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, offers.size());
        assertTrue("pickup points offered by the service", 
        		asList(501L,502L).stream().allMatch(pickupPoints::contains));
        assertTrue("The warehouse that will provide the stocks", 
        		asList(503L).stream().allMatch(shops::contains));
		var shipment = offers.get(0).getShipments().get(0);
		var eta = shipment.getEta();
        assertEquals(now().plusDays(ETA_FROM).toLocalDate(), eta.getFrom().toLocalDate());
		assertEquals(now().plusDays(ETA_TO).toLocalDate(), eta.getTo().toLocalDate());
	}
	
	
	
	
	
	@Test
	public void createDeliveryTest() {
		var service = shippingServiceFactory
									.getShippingService(SERVICE_ID, createServiceParams())
									.get();

		var details = createShippingsDetails();

		var tracker = service.requestShipment(details).collectList().block().get(0);
		
		assertNull(tracker.getShipmentExternalId());
		assertNull(tracker.getTracker());
		assertNotNull(tracker.getAirwayBillFile());
		assertFalse(tracker.getAirwayBillFile().isEmpty());
	}
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	public void validateShipmentItemsFromMultipleShops() {
		var service =
				shippingServiceFactory
				.getShippingService(SERVICE_ID, createServiceParams())
				.get();

		var details = createShippingsDetailsFromMultipleShops();
		
		service.validateShipment(details);
	}
	
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	public void validateShipmentItemsNotFromWarehouse() {
		var service = shippingServiceFactory
				.getShippingService(SERVICE_ID, createServiceParams())
				.get();

		var details = createShippingsDetailsNotFromWarehouse();
		
		service.validateShipment(details);
	}
	
	
	
	
	
	@Test(expected = RuntimeBusinessException.class)
	public void validateShipmentItemsNotFromAllowedShop() {
		var service = shippingServiceFactory
				.getShippingService(SERVICE_ID, createServiceParams())
				.get();

		var details = createShippingsDetailsNotFromAllowedShop();
		
		service.validateShipment(details);
	}
	
	
	
	
	
	private List<ShippingDetails> createShippingsDetailsNotFromAllowedShop() {
		var shippingDetails = createShippingsDetails();
		shippingDetails
		.stream()
		.map(ShippingDetails::getAdditionalData)
		.forEach(additionalData -> additionalData.put(SHOP_ID, "504"));
		return shippingDetails;
	}





	private List<ShippingDetails> createShippingsDetailsNotFromWarehouse() {
		var shippingDetails = createShippingsDetails();
		shippingDetails
		.stream()
		.peek(details -> details.setShopId(501L))
		.map(ShippingDetails::getItems)
		.flatMap(List::stream)
		.forEach(item -> item.setStockId(604L));
		return shippingDetails;
	}





	private List<ShippingDetails> createShippingsDetailsFromMultipleShops() {
		var shippingDetails = createShippingsDetails().get(0);
		var fromAnotherShop = objectMapper.convertValue(shippingDetails, ShippingDetails.class);
		fromAnotherShop.getItems().forEach(item -> item.setStockId(604L));
		
		return asList(shippingDetails, fromAnotherShop);
	}





	private List<ServiceParameter> createServiceParams() {
		return asList(new ServiceParameter(WAREHOUSE_ID,"503")
				, new ServiceParameter(PICKUP_POINTS_ID_LIST, "[501,502]")
				, new ServiceParameter(ETA_DAYS_MIN, ETA_FROM.toString())
				, new ServiceParameter(ETA_DAYS_MAX, ETA_TO.toString()));
	}
	
	
	private List<Long> getOfferedStockShops(List<ShippingOfferDTO> offers) {
		return offers
				.stream()
				.map(ShippingOfferDTO::getShipments)
				.flatMap(List::stream)
				.map(ShipmentDTO::getShopId)
				.collect(toList());
	}






	private List<Long> getOfferedShops(List<ShippingOfferDTO> offers) {
		return offers
				.stream()
				.map(ShippingOfferDTO::getAdditionalData)
				.flatMap(List::stream)
				.map(ShippingAdditionalDataDTO::getOptions)
				.flatMap(List::stream)
				.map(option -> Long.valueOf(option))
				.collect(toList());
	}
	
	
	
	private List<ShippingDetails> createShippingsDetails() {
		var customerAddr = new ShippingAddress();
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

		var item1 = new ShipmentItems(601L);
        item1.setName("Cool And Pool");
        item1.setBarcode("13AB");
        item1.setProductCode("Coco");
        item1.setSpecs("Cool/XXXL");

		var item2 = new ShipmentItems(602L);
        item2.setName("Sponge And Bob");
        item2.setBarcode("447788888888");
        item2.setProductCode("Bob");
        item2.setSpecs("Wet/S");

		var itemsOfShop1 = asList(item1 , item2);

		var additionalData =
				MapBuilder
				.<String,String>map()
				.put(SHOP_ID, "502")
				.getMap();

		var shippingDetails1 = new ShippingDetails();
		shippingDetails1.setDestination(customerAddr);
		shippingDetails1.setSource(shopAddr1);
		shippingDetails1.setItems(itemsOfShop1);
		shippingDetails1.setReceiver(receiver);
		shippingDetails1.setShopId(503L);
		shippingDetails1.setAdditionalData(additionalData);
		shippingDetails1.setMetaOrderId(145L);
		shippingDetails1.setSubOrderId(100L);


		var details = asList(shippingDetails1);
		return details;
	}
}





