package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.ProductCollectionRepository;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.VariantDTO;
import com.nasnav.persistence.ProductCollectionEntity;
import com.nasnav.persistence.ProductCollectionItemEntity;
import com.nasnav.persistence.ProductVariantsEntity;
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
import java.util.Set;

import static com.nasnav.commons.utils.CollectionUtils.concat;
import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Collection_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class CollectionsApiTest {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ProductCollectionRepository collectionRepo;


    @Test
    public void updateCollectionElements(){
        ProductCollectionEntity collectionBefore = collectionRepo.findByCollectionId(1001L).get();
        Set<Long> variantsBefore = getItemsIds(collectionBefore);
        assertTrue(setOf(310002L, 310003L).containsAll(variantsBefore));

        List<Long> newItems = asList(310002L, 310003L, 310004L);
        String requestJson =
                json()
                .put("product_id", 1001L)
                .put("operation", "update")
                .put("variant_ids", newItems)
                .toString();

        HttpEntity<?> request =  getHttpEntity(requestJson , "131415");

        ResponseEntity<String> response =
                template.exchange("/product/collection/element"
                        , POST
                        , request
                        , String.class);

        assertEquals(OK, response.getStatusCode());

        ProductCollectionEntity collectionAfter = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsAfter = getItemsIdsSortedByPriority(collectionAfter);

        assertEquals("items priority should be the same as their insertion order", newItems, variantsAfter);
    }




    @Test
    public void getCollections(){
        ResponseEntity<ProductDetailsDTO> response =
                template.getForEntity("/navbox/collection?id=1001", ProductDetailsDTO.class);

        assertEquals(OK, response.getStatusCode());

        List<Long> expectedItemsSorted = asList(310003L, 310002L);
        List<Long> retrievedItems = response.getBody().getVariants().stream().map(VariantDTO::getId).collect(toList());

        assertEquals("collection items should be sorted by priority", expectedItemsSorted, retrievedItems);
    }




    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Collection_Test_Data_2.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getCollectionsWithNullPriorityItems(){
        ResponseEntity<ProductDetailsDTO> response =
                template.getForEntity("/navbox/collection?id=1001", ProductDetailsDTO.class);

        assertEquals(OK, response.getStatusCode());

        List<Long> expectedItemsSorted = asList(310003L, 310002L);
        List<Long> retrievedItems = response.getBody().getVariants().stream().map(VariantDTO::getId).collect(toList());

        assertEquals("collection items should be sorted by priority, null priorities are set to 0", expectedItemsSorted, retrievedItems);
    }



    @Test
    public void deleteElementsFromCollection(){
        ProductCollectionEntity collectionBefore = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsBefore = getItemsIdsSortedByPriority(collectionBefore);
        List<Long> expectedOldItems = asList(310003L, 310002L);
        assertEquals(expectedOldItems, variantsBefore);

        List<Long> itemsToDelete = asList(310003L);
        String requestJson =
                json()
                    .put("product_id", 1001L)
                    .put("operation", "delete")
                    .put("variant_ids", itemsToDelete)
                    .toString();

        HttpEntity<?> request =  getHttpEntity(requestJson , "131415");

        ResponseEntity<String> response =
                template.exchange("/product/collection/element"
                        , POST
                        , request
                        , String.class);

        assertEquals(OK, response.getStatusCode());

        ProductCollectionEntity collectionAfter = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsAfter = getItemsIdsSortedByPriority(collectionAfter);
        List<Long> expectedNewItems = asList(310002L);
        assertEquals( expectedNewItems, variantsAfter);
    }



    @Test
    public void addElementsToCollections(){
        ProductCollectionEntity collectionBefore = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsBefore = getItemsIdsSortedByPriority(collectionBefore);
        List<Long> expectedOldItems = asList(310003L, 310002L);
        assertEquals(expectedOldItems, variantsBefore);

        List<Long> newItems = asList(310006L, 310005L);
        String requestJson =
                json()
                    .put("product_id", 1001L)
                    .put("operation", "add")
                    .put("variant_ids", newItems)
                    .toString();

        HttpEntity<?> request =  getHttpEntity(requestJson , "131415");

        ResponseEntity<String> response =
                template.exchange("/product/collection/element"
                        , POST
                        , request
                        , String.class);

        assertEquals(OK, response.getStatusCode());

        ProductCollectionEntity collectionAfter = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsAfter = getItemsIdsSortedByPriority(collectionAfter);
        List<Long> expectedNewItems = concat(expectedOldItems, newItems);
        assertEquals("new added item will be appended after the other items", expectedNewItems, variantsAfter);
    }




    @Test
    public void addExistingElementsToCollections(){
        ProductCollectionEntity collectionBefore = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsBefore = getItemsIdsSortedByPriority(collectionBefore);
        List<Long> expectedOldItems = asList(310003L, 310002L);
        assertEquals(expectedOldItems, variantsBefore);

        List<Long> newItems = asList(310006L, 310005L);
        List<Long> newItemsWithExistingItem = concat(newItems, expectedOldItems);
        String requestJson =
                json()
                    .put("product_id", 1001L)
                    .put("operation", "add")
                    .put("variant_ids", newItemsWithExistingItem)
                    .toString();

        HttpEntity<?> request =  getHttpEntity(requestJson , "131415");

        ResponseEntity<String> response =
                template.exchange("/product/collection/element"
                        , POST
                        , request
                        , String.class);

        assertEquals(OK, response.getStatusCode());

        ProductCollectionEntity collectionAfter = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsAfter = getItemsIdsSortedByPriority(collectionAfter);
        List<Long> expectedNewItems = concat(expectedOldItems, newItems);
        assertEquals("old items will be ignored", expectedNewItems, variantsAfter);
    }



    @Test
    public void addRepeatedElementsToCollections(){
        ProductCollectionEntity collectionBefore = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsBefore = getItemsIdsSortedByPriority(collectionBefore);
        List<Long> expectedOldItems = asList(310003L, 310002L);
        assertEquals(expectedOldItems, variantsBefore);

        List<Long> newItems = asList(310006L, 310005L);
        List<Long> newItemsWithRepeatedItems = concat(newItems, newItems);
        String requestJson =
                json()
                    .put("product_id", 1001L)
                    .put("operation", "add")
                    .put("variant_ids", newItemsWithRepeatedItems)
                    .toString();

        HttpEntity<?> request =  getHttpEntity(requestJson , "131415");

        ResponseEntity<String> response =
                template.exchange("/product/collection/element"
                        , POST
                        , request
                        , String.class);

        assertEquals(OK, response.getStatusCode());

        ProductCollectionEntity collectionAfter = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsAfter = getItemsIdsSortedByPriority(collectionAfter);
        List<Long> expectedNewItems = concat(expectedOldItems, newItems);
        assertEquals("new repeated items will be ignored", expectedNewItems, variantsAfter);
    }




    @Test
    public void addNonExistentElementsToCollections(){
        ProductCollectionEntity collectionBefore = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsBefore = getItemsIdsSortedByPriority(collectionBefore);
        List<Long> expectedOldItems = asList(310003L, 310002L);
        assertEquals(expectedOldItems, variantsBefore);

        List<Long> newItems = asList(-1L, 310005L);
        String requestJson =
                json()
                    .put("product_id", 1001L)
                    .put("operation", "add")
                    .put("variant_ids", newItems)
                    .toString();

        HttpEntity<?> request =  getHttpEntity(requestJson , "131415");

        ResponseEntity<String> response =
                template.exchange("/product/collection/element"
                        , POST
                        , request
                        , String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());

        ProductCollectionEntity collectionAfter = collectionRepo.findByCollectionId(1001L).get();
        List<Long> variantsAfter = getItemsIdsSortedByPriority(collectionAfter);
        assertEquals("no changes happens to the collection", expectedOldItems, variantsAfter);
    }





    private Set<Long> getItemsIds(ProductCollectionEntity collectionBefore) {
        return collectionBefore
                .getVariants()
                .stream()
                .map(ProductVariantsEntity::getId)
                .collect(toSet());
    }



    private List<Long> getItemsIdsSortedByPriority(ProductCollectionEntity collectionBefore) {
        return collectionBefore
                .getItems()
                .stream()
                .sorted(comparing(ProductCollectionItemEntity::getPriority))
                .map(ProductCollectionItemEntity::getItem)
                .map(ProductVariantsEntity::getId)
                .collect(toList());
    }
}
