package com.nasnav.test.shipping.services.fixed_fee_same_city;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.nasnav.shipping.services.FixedFeeStrictSameCityShippingService.*;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
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
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shipping_Test_Data_12.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class FixedFeeSameCityShippingServiceTest extends AbstractTestWithTempBaseDir {

    private static final Integer ETA_FROM = 1;
    private static final Integer ETA_TO = 2;

    @Autowired
    private ShippingServiceFactory shippingServiceFactory;


    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper objectMapper;




    @Test
    public void testGetOffer() throws IOException {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response =
                template.exchange("/shipping/offers?customer_address=12300001", GET, request, String.class);

        assertEquals(OK, response.getStatusCode());

        List<ShippingOfferDTO> offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
        boolean allOffersAreForFixedFeeService =
                offers.stream().allMatch(offer -> Objects.equals(offer.getServiceId(), SERVICE_ID));
        List<ShipmentDTO> shipments = offers.get(0).getShipments();

        sort(shipments, comparing(ShipmentDTO::getShippingFee));
        assertTrue(allOffersAreForFixedFeeService);
        assertEquals(3, shipments.size());
        assertEquals(0, shipments.get(0).getShippingFee().compareTo(new BigDecimal("5")));
        assertEquals(0, shipments.get(1).getShippingFee().compareTo(new BigDecimal("5")));
        assertEquals(0, shipments.get(2).getShippingFee().compareTo(new BigDecimal("5")));
        assertEquals(now().plusDays(1).toLocalDate() , shipments.get(0).getEta().getFrom().toLocalDate());
        assertEquals(now().plusDays(2).toLocalDate() , shipments.get(0).getEta().getTo().toLocalDate());
    }



    @Test
    public void testGetOfferCustomerFromAnotherCity() throws IOException {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<String> response =
                template.exchange("/shipping/offers?customer_address=12300003", GET, request, String.class);

        assertEquals(OK, response.getStatusCode());

        List<ShippingOfferDTO> offers =
                objectMapper.readValue(response.getBody(), new TypeReference<List<ShippingOfferDTO>>(){});
        assertTrue(offers.isEmpty());
    }




    @Test
    public void createDeliveryTest() {
        ShippingService service = shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();

        ShipmentTracker tracker = service.requestShipment(details).collectList().block().get(0);

        assertNull(tracker.getShipmentExternalId());
        assertNull(tracker.getTracker());
        assertNull(tracker.getAirwayBillFile());
    }



    @Test(expected = RuntimeBusinessException.class)
    public void createDeliveryCustomerFromAnotherCityTest() {
        ShippingService service = shippingServiceFactory
                .getShippingService(SERVICE_ID, createServiceParams())
                .get();

        List<ShippingDetails> details = createShippingsDetails();
        setShopInAnotherCity(details.get(0));

        service.requestShipment(details).collectList().block().get(0);
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
    public void testGetOfferCustomerAndShopNotInSameCity() {
        ShippingService service =
                shippingServiceFactory
                        .getShippingService(SERVICE_ID, createServiceParams())
                        .get();

        List<ShippingDetails> details = createShippingsDetails();
        setShopInAnotherCity(details.get(0));

        Mono<ShippingOffer> offer = service.createShippingOffer(details);
        assertFalse(offer.blockOptional().isPresent());
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



    private void setShopInAnotherCity(ShippingDetails details) {
        ShippingAddress shopAddr = new ShippingAddress();
        shopAddr.setAddressLine1("pikutcho st.");
        shopAddr.setArea(171717L);
        shopAddr.setBuildingNumber("555");
        shopAddr.setCity(1L);
        shopAddr.setCountry(1L);
        shopAddr.setFlatNumber("5A");
        shopAddr.setId(12300001L);
        shopAddr.setName("Ash ketchum");
        details.setSource(shopAddr);
    }





    private List<ServiceParameter> createServiceParams() {
        return asList(new ServiceParameter(MIN_SHIPPING_FEE,"15")
                , new ServiceParameter(SUPPORTED_CITIES, "[1,2]")
                , new ServiceParameter(ETA_DAYS_MIN, ETA_FROM.toString())
                , new ServiceParameter(ETA_DAYS_MAX, ETA_TO.toString()));
    }



    private List<ShippingDetails> createShippingsDetails() {
        ShippingAddress customerAddr = new ShippingAddress();
        customerAddr.setAddressLine1("Mama st.");
        customerAddr.setArea(181818L);
        customerAddr.setBuildingNumber("555");
        customerAddr.setCity(2L);
        customerAddr.setCountry(1L);
        customerAddr.setFlatNumber("5A");
        customerAddr.setId(12300001L);
        customerAddr.setName("Hamada Ezzo");


        ShippingAddress shopAddr1 = new ShippingAddress();
        shopAddr1.setAddressLine1("Food court st.");
        shopAddr1.setArea(181818L);
        shopAddr1.setBuildingNumber("777");
        shopAddr1.setCity(2L);
        shopAddr1.setCountry(1L);
        shopAddr1.setId(12300002L);
        shopAddr1.setName("7rnksh Nasnav");


        ShippingAddress shopAddr2 = new ShippingAddress();
        shopAddr2.setAddressLine1("Food court st.");
        shopAddr2.setArea(181818L);
        shopAddr2.setBuildingNumber("888");
        shopAddr2.setCity(2L);
        shopAddr2.setCountry(1L);
        shopAddr2.setId(12300003L);
        shopAddr2.setName("Freska Nasnav");

        ShipmentReceiver receiver = new ShipmentReceiver();
        receiver.setFirstName("Sponge");
        receiver.setLastName("Bob");
        receiver.setPhone("01000000000");

        ShipmentItems item1 = new ShipmentItems(601L);
        item1.setName("Cool And Pool");
        item1.setBarcode("13AB");
        item1.setProductCode("Coco");
        item1.setSpecs("Cool/XXXL");

        ShipmentItems item2 = new ShipmentItems(602L);
        item2.setName("Sponge And Bob");
        item2.setBarcode("447788888888");
        item2.setProductCode("Bob");
        item2.setSpecs("Wet/S");

        List<ShipmentItems> itemsOfShop1 = asList(item1 , item2);


        ShippingDetails shippingDetails1 = new ShippingDetails();
        shippingDetails1.setDestination(customerAddr);
        shippingDetails1.setSource(shopAddr1);
        shippingDetails1.setItems(itemsOfShop1);
        shippingDetails1.setReceiver(receiver);
        shippingDetails1.setShopId(503L);
        shippingDetails1.setMetaOrderId(145L);
        shippingDetails1.setSubOrderId(100L);


        List<ShippingDetails> details = asList(shippingDetails1);
        return details;
    }

}
