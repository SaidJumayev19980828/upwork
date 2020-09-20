package com.nasnav.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.ReturnRequestItemRepository;
import com.nasnav.dao.ReturnRequestRepository;
import com.nasnav.dao.ReturnShipmentRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.*;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.enumerations.ReturnRequestStatus.*;
import static com.nasnav.enumerations.ShippingStatus.REQUSTED;
import static com.nasnav.test.commons.TestCommons.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Orders_Test_Data_Insert_11.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class OrderReturnNoEmailMockTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepo;

    @Autowired
    private ReturnRequestRepository returnRequestRepo;


    @Autowired
    private ReturnShipmentRepository returnShipmentRepo;



	private JSONObject createReturnRequestBody() {
		JSONArray returnedItems = 
        		jsonArray()
                .put(json()
                        .put("order_item_id", 330037)
                        .put("returned_quantity", 1))
                .put(json()
                        .put("order_item_id", 330036)
                        .put("returned_quantity", 1));
        return json().put("item_list", returnedItems);
	}




	@Test
    public void orderReturnFullCycle() throws InterruptedException {
        Long returnRequestId = createReturnRequest();
        confirmReturnRequest(returnRequestId);
        acceptReturnRequest(returnRequestId);

        Thread.sleep(1000);
    }




    private Long createReturnRequest() {
        JSONObject body = createReturnRequestBody();

        HttpEntity<?> request = getHttpEntity(body.toString(), "123");
        ResponseEntity<Long> response = template.postForEntity("/order/return", request, Long.class);

        assertEquals(OK, response.getStatusCode());

        Long returnRequestId = response.getBody();
        Optional<ReturnRequestEntity> entity = returnRequestRepo.findByReturnRequestId(returnRequestId, 99001L);

        checkReturnRequestData(entity);
        assertReturnRequestItemsCreated(body, entity);
        return returnRequestId;
    }



    private void confirmReturnRequest(Long returnRequestId) throws InterruptedException {
        ReturnRequestEntity entityBefore = returnRequestRepo.findById(returnRequestId).get();
        assertEquals(NEW.getValue(), entityBefore.getStatus());
        //-------------
        HttpEntity<?> confirmRequest = getHttpEntity("131415");

        ResponseEntity<String> res =
                template.postForEntity("/order/return/confirm?id="+ returnRequestId, confirmRequest, String.class);
        Thread.sleep(1000);
        //--------------
        assertReturnShipmentsCreated(returnRequestId, res);
    }




    private void acceptReturnRequest(Long returnRequestId) {
        JSONObject receiveRequestBody =
               returnRequestRepo
               .findByReturnRequestId(returnRequestId, 99001L)
                .get()
                    .getReturnedItems()
                    .stream()
                    .map(this::toReturnedItemJson)
                    .collect(
                            collectingAndThen(
                                    toList()
                                    , this::toJsonArrayOfReturnedItems));

        List<Integer> oldStockQuantities =  getStockQuantities();
        //--------------
        HttpEntity<?> receiveRequest = getHttpEntity(receiveRequestBody.toString(), "131415");

        ResponseEntity<String> receiveResponse =
                template.postForEntity("/order/return/received_item", receiveRequest, String.class);
        //--------------
        assertEquals(200, receiveResponse.getStatusCodeValue());

        assertRequestStatus();
        assertStocksIncremented(oldStockQuantities);
    }



    private void assertRequestStatus() {
        List<ReturnRequestItemEntity> items =  returnRequestItemRepo.findByBasket_IdIn(asList(330036L, 330037L));
        Integer requestStatus = getRequestStatus(items);
        assertEquals(RECEIVED.getValue() , requestStatus);
    }



    private void assertStocksIncremented(List<Integer> oldStockQuantities) {
	    int returnedQtyCommon = 1;
        List<Integer> newStockQuantities =  getStockQuantities();
        for(int i=0; i<newStockQuantities.size(); i++){
            assertEquals(oldStockQuantities.get(i) + returnedQtyCommon, newStockQuantities.get(i).intValue());
        }
    }



    private Integer getRequestStatus(List<ReturnRequestItemEntity> items) {
        return items
                .stream()
                .findFirst()
                .map(ReturnRequestItemEntity::getReturnRequest)
                .map(ReturnRequestEntity::getStatus)
                .orElse(-1);
    }





    private List<Integer> getStockQuantities() {
        return stockRepository
                .findByIdInOrderById(asList(601L, 604L))
                .stream()
                .map(StocksEntity::getQuantity)
                .collect(toList());
    }


    private JSONObject toJsonArrayOfReturnedItems(List<JSONObject> itemJsonList) {
        JSONArray arr = new JSONArray();
        itemJsonList.forEach(arr::put);
	    return json().put("returned_items", arr);

    }



    private JSONObject toReturnedItemJson(ReturnRequestItemEntity entity) {
        return  json()
                .put("return_request_item_id", entity.getId())
                .put("received_quantity", entity.getReturnedQuantity());
    }







    private void checkReturnRequestData(Optional<ReturnRequestEntity> entity) {
        assertTrue(entity.isPresent());
        assertEquals(NEW.getValue() , entity.get().getStatus());
        assertNull(entity.get().getCreatedByEmployee());
        assertEquals(88L, entity.get().getCreatedByUser().getId().longValue());
        assertNotNull(entity.get().getCreatedOn());
        assertEquals(310004L, entity.get().getMetaOrder().getId().longValue());
    }



    private void assertReturnRequestItemsCreated(JSONObject body, Optional<ReturnRequestEntity> entity) {
        Set<ReturnRequestItemEntity> returnedItems = entity.get().getReturnedItems();
        List<Long> ids =
                returnedItems
                        .stream()
                        .map(ReturnRequestItemEntity::getBasket)
                        .map(BasketsEntity::getId)
                        .collect(toList());
        List<Integer> quantities =
                returnedItems
                        .stream()
                        .map(ReturnRequestItemEntity::getReturnedQuantity)
                        .collect(toList());
        assertEquals(body.getJSONArray("item_list").length(), returnedItems.size());
        assertTrue("expected Returned quantities", asList(1,2).containsAll(quantities));
        assertTrue("expected Basket items ids", asList(330037L, 330036L).containsAll(ids));
    }




    private void assertReturnShipmentsCreated(Long id, ResponseEntity<String> res) {
        assertEquals(200, res.getStatusCodeValue());

        ReturnRequestEntity entity = returnRequestRepo.findById(id).get();
        assertEquals(CONFIRMED.getValue(), entity.getStatus());

        List<ReturnShipmentEntity> shipments = returnShipmentRepo.findByReturnRequest_Id(id);
        assertEquals("each item was from different shop, so each will have " +
                "separate shipment , the same as how they are shipped to customer"
                ,2, shipments.size());

        assertTrue("each item was from different shop, so each will have " +
                        "separate shipment , the same as how they are shipped to customer"
                ,isAllHasSingleItem(shipments));
        assertTrue(isAllHasRequestedStatus(shipments));
        assertTrue( isAllHasTrackingData(shipments));
    }



    private boolean isAllHasSingleItem(List<ReturnShipmentEntity> shipments) {
        return shipments
                .stream()
                .map(ReturnShipmentEntity::getReturnRequestItems)
                .allMatch(itms -> itms.size() == 1);
    }



    private boolean isAllHasRequestedStatus(List<ReturnShipmentEntity> shipments) {
        return shipments
                .stream()
                .allMatch(shp -> REQUSTED.getValue().equals(shp.getStatus()));
    }



    private boolean isAllHasTrackingData(List<ReturnShipmentEntity> shipments) {
        return shipments
                .stream()
                .allMatch(shp -> noneIsNull(shp.getExternalId(), shp.getTrackNumber(), shp.getShippingServiceId()));
    }
}
