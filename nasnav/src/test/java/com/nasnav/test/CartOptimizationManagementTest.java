package com.nasnav.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.SettingRepository;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.service.cart.optimizers.CartOptimizationStrategy;
import com.nasnav.shipping.services.PickupPointsWithInternalLogistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.SAME_CITY;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SHOP_PER_SUBAREA;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_3.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class CartOptimizationManagementTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private SettingRepository settingRepo;

    @Autowired
    private OrganizationCartOptimizationRepository optimizationRepo;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void postCartOptimizationSettingNoAuthZTest() {
        HttpEntity<?> req = getHttpEntity("eereeee");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
    }




    @Test
    public void postCartOptimizationSettingNoAuthNTest() {
        HttpEntity<?> req = getHttpEntity("NOT EXIST");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
    }




    @Test
    public void postCartOptimizationSettingInvalidParamTest() {
        var strategy = "INVALID";
        var body =
                json()
                        .put("strategy_name", strategy)
                        .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }




    @Test
    public void postCartOptimizationSettingSuccessTest() {
        var strategy = SAME_CITY.name();
        var optimizationParams = json().toString();
        var body =
                json()
                    .put("strategy_name", strategy)
                    .toString();
        postOptimizationStrategy(body);

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertTrue(optimizationParamsEntity.isPresent());
        assertEquals(strategy, optimizationParamsEntity.get().getOptimizationStrategy());
        assertEquals(optimizationParams, optimizationParamsEntity.get().getParameters());
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_4.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void updateCartOptimizationSettingSuccessTest() {
        var strategy = SAME_CITY.name();
        final var shippingService = "FIXED_FEE";

        assertOptStrategyExist(strategy, shippingService);

        var optimizationParams = json().put("DUMMY", "VAL");
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("shipping_service_id", shippingService)
                    .put("parameters", optimizationParams)
                    .toString();
        postOptimizationStrategy(body);

        var optimizationParamsAfter =
                optimizationRepo.findByOptimizationStrategyAndShippingServiceIdAndOrganization_Id(strategy, shippingService, 99001L);
        assertEquals(1, optimizationParamsAfter.size());
        assertEquals(optimizationParams.toString(), optimizationParamsAfter.get(0).getParameters());
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_4.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void updateDefaultCartOptimizationSettingSuccessTest() {
        var strategy = SAME_CITY.name();

        assertOptStrategyExist(strategy);

        var optimizationParams = json().put("DUMMY", "VAL");
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters", optimizationParams)
                    .toString();
        postOptimizationStrategy(body);

        var optimizationParamsAfter =
                optimizationRepo.findOrganizationDefaultOptimizationStrategy(99001L);
        assertTrue(optimizationParamsAfter.isPresent());
        assertEquals(optimizationParams.toString(), optimizationParamsAfter.get().getParameters());
    }



    private void postOptimizationStrategy(String body) {
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(OK, res.getStatusCode());
    }


    private void assertOptStrategyExist(String strategy, String shippingService) {
        var optimizationParamsBefore =
                optimizationRepo.findByOptimizationStrategyAndShippingServiceIdAndOrganization_Id(strategy, shippingService , 99001L);
        assertEquals(1, optimizationParamsBefore.size());
        assertEquals("{}", optimizationParamsBefore.get(0).getParameters());
    }


    private void assertOptStrategyExist(String strategy) {
        var optimizationParamsBefore =
                optimizationRepo.findOrganizationDefaultOptimizationStrategy( 99001L);
        assertTrue(optimizationParamsBefore.isPresent());
        assertEquals("{}", optimizationParamsBefore.get().getParameters());
    }


    @Test
    public void postWarehouseCartOptimizationSettingInvalidParamTest() {
        var strategy = WAREHOUSE;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters", json())
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }





    @Test
    public void postWarehouseCartOptimizationSettingNoExistingWareHouseTest() {
        var strategy = WAREHOUSE;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters", json().put("warehouse_id", -1))
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }



    @Test
    public void postWarehouseCartOptimizationSettingWarehouseFromAnotherOrgTest() {
        var strategy = WAREHOUSE;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters", json().put("warehouse_id", 501))
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }



    @Test
    public void postSubAreaShopCartOptimizationSettingDefaultShopFromAnotherOrgTest() {
        var strategy = SHOP_PER_SUBAREA;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters",
                            json()
                                    .put("default_shop", 501)
                                    .put("sub_area_shop_mapping", json().put("77001", 502)))
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }



    @Test
    public void postSubAreaShopCartOptimizationSettingSubAreaShopFromAnotherOrgTest() {
        var strategy = SHOP_PER_SUBAREA;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters",
                            json()
                                    .put("default_shop", 502)
                                    .put("sub_area_shop_mapping", json().put("77001", 501)))
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }



    @Test
    public void postSubAreaShopCartOptimizationSettingSubAreaFromAnotherOrgTest() {
        var strategy = SHOP_PER_SUBAREA;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters",
                            json()
                                    .put("default_shop", 502)
                                    .put("sub_area_shop_mapping", json().put("77002", 502)))
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertFalse(optimizationParamsEntity.isPresent());
    }



    @Test
    public void postSubAreaShopCartOptimizationSettingSuccessTest() {
        var strategy = SHOP_PER_SUBAREA;
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters",
                            json()
                                    .put("default_shop", 502)
                                    .put("sub_area_shop_mapping", json().put("77001", 502)))
                    .toString();
        postOptimizationStrategy(body);

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertTrue(optimizationParamsEntity.isPresent());
        assertEquals(strategy, optimizationParamsEntity.get().getOptimizationStrategy());
    }



    @Test
    public void postWarehouseCartOptimizationSettingSuccessTest() {
        var strategy = WAREHOUSE;
        var optimizationParams = json().put("warehouse_id", 502);
        var body =
                json()
                    .put("strategy_name", strategy)
                    .put("parameters", optimizationParams)
                    .toString();
        postOptimizationStrategy(body);

        var optimizationParamsEntity =
                optimizationRepo.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, 99001L);
        assertTrue(optimizationParamsEntity.isPresent());
        assertEquals(strategy, optimizationParamsEntity.get().getOptimizationStrategy());
        assertEquals(optimizationParams.toString(), optimizationParamsEntity.get().getParameters());
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getOptimizationStrategyTest() throws JsonParseException, Exception {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategy",GET, req, String.class);
        assertEquals(OK, res.getStatusCode());

        List<CartOptimizationSettingDTO> strategyConfigs =
                objectMapper.readValue(res.getBody(), new TypeReference<>() {
                });

        assertEquals(2, strategyConfigs.size());
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getOptimizationStrategyNoAuthzTest() throws JsonParseException, Exception {
        HttpEntity<?> req = getHttpEntity("eereeee");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategy",GET, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getOptimizationStrategyNoAuthNTest() throws JsonParseException, Exception {
        HttpEntity<?> req = getHttpEntity("Non existing");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategy",GET, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
    }



    @Test
    public void listOptimizationStrategiesTest() throws JsonParseException, Exception {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategies",GET, req, String.class);
        assertEquals(OK, res.getStatusCode());

        List<CartOptimizationStrategyDTO> strategies =
                objectMapper.readValue(res.getBody(), new TypeReference<>() {
                });


        var strategiesNames =
                stream(CartOptimizationStrategy.values())
                        .map(CartOptimizationStrategy::getValue)
                        .collect(toSet());

        var allStrategiesReturned =
                strategies
                        .stream()
                        .map(CartOptimizationStrategyDTO::getName)
                        .allMatch(strategiesNames::contains);


        assertEquals(strategiesNames.size(), strategies.size());
        assertTrue(allStrategiesReturned);
    }



    @Test
    public void listOptimizationStrategiesNoAuthzTest() throws JsonParseException, Exception {
        HttpEntity<?> req = getHttpEntity("eereeee");
        var res =
                template
                    .exchange("/organization/settings/cart_optimization/strategies",GET, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
    }



    @Test
    public void listOptimizationStrategiesAuthNTest() throws JsonParseException, Exception {
        HttpEntity<?> req = getHttpEntity("Non existing");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategies",GET, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
    }



    @Test
    public void deleteCartOptimizationSettingNoAuthZTest() {
        HttpEntity<?> req = getHttpEntity("eereeee");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",DELETE, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
    }



    @Test
    public void deleteCartOptimizationSettingNoAuthNTest() {
        HttpEntity<?> req = getHttpEntity("NOT EXIST");
        var res =
                template
                        .exchange("/organization/settings/cart_optimization/strategy",DELETE, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_4.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void deleteCartOptimizationSettingForOrgTest() {
        var strategy = SAME_CITY.name();
        var existsBefore = optimizationRepo.findOrganizationDefaultOptimizationStrategy(99001L).isPresent();
        assertTrue(existsBefore);
        var body =
                json()
                    .put("strategy_name", strategy)
                    .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange(String.format("/organization/settings/cart_optimization/strategy?strategy_name=%s", strategy),DELETE, req, String.class);
        assertEquals(OK, res.getStatusCode());

        var existsAfter = optimizationRepo.findOrganizationDefaultOptimizationStrategy(99001L).isPresent();
        assertFalse(existsAfter);
    }



    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_4.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void deleteCartOptimizationSettingForShippingServiceTest() {
        var strategy = WAREHOUSE;
        var shipping = PickupPointsWithInternalLogistics.SERVICE_ID;
        var existsBefore = optimizationRepo.findFirstByOptimizationStrategyAndShippingServiceIdAndOrganization_IdOrderByIdDesc(strategy, shipping, 99001L).isPresent();
        assertTrue(existsBefore);
        var body =
                json()
                        .put("strategy_name", strategy)
                        .toString();
        HttpEntity<?> req = getHttpEntity(body, "hijkllm");
        var res =
                template
                        .exchange(String.format("/organization/settings/cart_optimization/strategy?strategy_name=%s&shipping_service=%s", strategy,shipping)
                                ,DELETE, req, String.class);
        assertEquals(OK, res.getStatusCode());

        var existsAfter = optimizationRepo.findFirstByOptimizationStrategyAndShippingServiceIdAndOrganization_IdOrderByIdDesc(strategy, shipping,99001L).isPresent();
        assertFalse(existsAfter);
    }
}
