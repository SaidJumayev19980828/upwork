package com.nasnav.test;


import com.nasnav.dao.FilesRepository;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.service.ThreeDModelService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/test_data_for_3d_model.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/truncate_tables_before_test_case.sql"})
class ThreeDModelApiTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private FilesRepository filesRepository;
    @Value("classpath:/files/product__list_upate.csv")
    private Resource file;
    @Autowired
    private ThreeDModelService threeDModelService;

    @Test
    void addNewThreeDModel() {
        String body = createThreeDModelRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("files", file);
        HttpEntity<Object> json = getHttpEntity(map, "hijkllm", MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        ResponseEntity<ThreeDModelResponse> response = template.postForEntity("/product/add/new3d/model", json, ThreeDModelResponse.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("test-barcode1", response.getBody().getBarcode());
        List<String> filesUrls = filesRepository.getUrlsByModelId(response.getBody().getModelId());
        assertEquals(filesUrls.size(), 1);
    }

    private JSONObject createThreeDModelRequestBody() {
        return json()
                .put("barcode", "test-barcode1")
                .put("name", "Alfa Romero")
                .put("sku", "sku-test1")
                .put("model", "model-test")
                .put("size", 30L)
                .put("description", "des-test")
                .put("color", "red");
    }

    @Test
    void searchByBarcode() {
        String barcode = "test-barcode2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/product/get3d/model?barcode=" + barcode, GET, request, ThreeDModelResponse.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(6, response.getBody().getSize());
    }

    @Test
    void searchBySKU() {
        String sku = "sku-test2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/product/get3d/model?sku=" + sku, GET, request, ThreeDModelResponse.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(6, response.getBody().getSize());
    }

    @Test
    void assignModelToProduct() {
        String sku = "sku-test2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/product/get3d/model?sku=" + sku, GET, request, ThreeDModelResponse.class);
        assertEquals(OK, response.getStatusCode());
        Long modelId = response.getBody().getModelId();
        ResponseEntity<String> producResponse = template.postForEntity("/product/assign/model/to/product?product_id=1001&model_id=" + modelId, request, String.class);
        assertEquals(OK, producResponse.getStatusCode());

    }

    @Test
    void getThreeDModelByBarcodeOrSKU() {
        String sku = "sku-test2";
        String barcode = "test-barcode2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/product/get3d/model?barcode="+barcode+"&sku=" + sku, GET, request, ThreeDModelResponse.class);
        assertEquals(response.getBody().getSku(), "sku-test2");
        assertEquals(response.getBody().getBarcode(), "test-barcode2");
    }

    @Test
    void getThreeDModel() {
        String sku = "sku-test2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/product/get3d/model?sku=" + sku, GET, request, ThreeDModelResponse.class);
        Long modelId=response.getBody().getModelId();
        ThreeDModelResponse modelResponse = threeDModelService.getThreeDModel(modelId);
        assertEquals(modelResponse.getModel(), response.getBody().getModel());
    }

    @Test
    void getThreeDModelMissingParamsError() {
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/product/get3d/model", GET, request, ThreeDModelResponse.class);
        assertEquals(response.getStatusCode(), BAD_REQUEST);
    }

    @Test
    void getAllThreeDModel() {
        HttpEntity<Object> request = getHttpEntity("testNonAuth");
        ResponseEntity<PaginatedResponse<ThreeDModelResponse>> response = template.exchange("/product/get3d/all", GET, request,
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }

}
