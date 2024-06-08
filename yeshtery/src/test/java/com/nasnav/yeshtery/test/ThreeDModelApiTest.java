package com.nasnav.yeshtery.test;

import com.nasnav.dao.FilesRepository;
import com.nasnav.dto.response.ThreeDModelList;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.service.ThreeDModelService;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.yeshtery.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/test_data_for_3d_model.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/truncate_tables_before_test_case.sql"})
class ThreeDModelApiTest extends AbstractTestWithTempBaseDir
{
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private FilesRepository filesRepository;
    @Value("classpath:/files/product__list_upate.csv")
    private Resource file;

    @Test
    void addNewThreeDModel() {
        String body = createThreeDModelRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("files", file);
        HttpEntity<Object> json = getHttpEntity(map, "hijkllm", MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        ResponseEntity<ThreeDModelResponse> response = template.postForEntity("/v1/product/model3d", json, ThreeDModelResponse.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("test-barcode", response.getBody().getBarcode());
        List<String> filesUrls = filesRepository.getUrlsByModelId(response.getBody().getModelId());
        assertEquals(1, filesUrls.size());
    }

    private JSONObject createThreeDModelRequestBody() {
        return json()
                .put("barcode", "test-barcode")
                .put("name", "Alfa Romero")
                .put("sku", "sku-test")
                .put("model", "model-test")
                .put("size", "30")
                .put("description", "des-test")
                .put("color", "red")
                .put("image", "image");
    }

    @Test
    void getAllThreeDModel() {
        ResponseEntity<ThreeDModelList> response = template.getForEntity("/v1/product/model3d/all", ThreeDModelList.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAllByParams() {
        String sku = "sku-test2";
        String barcode = "test-barcode2";
        String color = "blue";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelList> response = template.exchange("/v1/product/model3d/all?barcode=" + barcode + "&sku=" + sku + "&color=" + color, GET, request, ThreeDModelList.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).getThreeDModels().size());
    }

    @Test
    void getOneByBarcode() {
        String barcode = "test-barcode2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/v1/product/model3d/one?barcode=" + barcode, GET, request, ThreeDModelResponse.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(barcode, response.getBody().getBarcode());
    }

    @Test
    void getOneBySKU() {
        String sku = "sku-test2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/v1/product/model3d/one?sku=" + sku, GET, request, ThreeDModelResponse.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(sku, response.getBody().getSku());
    }

    @Test
    void assignModelToProduct() {
        String sku = "sku-test2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/v1/product/model3d/one?sku=" + sku, GET, request, ThreeDModelResponse.class);
        assertEquals(OK, response.getStatusCode());
        Long modelId = response.getBody().getModelId();
        ResponseEntity<String> producResponse = template.postForEntity("/v1/product/model3d/assign?product_id=1001&model_id=" + modelId, request, String.class);
        assertEquals(OK, producResponse.getStatusCode());

    }

    @Test
    void getOneByBarcodeAndSKU() {
        String sku = "sku-test2";
        String barcode = "test-barcode2";
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/v1/product/model3d/one?barcode=" + barcode + "&sku=" + sku, GET, request, ThreeDModelResponse.class);
        assertEquals(response.getBody().getSku(), sku);
        assertEquals(response.getBody().getBarcode(), barcode);
    }

    @Test
    void getOneWithMissingParamsError() {
        HttpEntity<Object> request = getHttpEntity("hijkllm");
        ResponseEntity<ThreeDModelResponse> response = template.exchange("/v1/product/model3d/one", GET, request, ThreeDModelResponse.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void update3DModel() {
        Long modelId = 24L;
        String updatedBody = createThreeDModelRequestBody().put("barcode", "updated-barcode").toString();
        MultiValueMap<String, Object> updatedMap = new LinkedMultiValueMap<>();
        updatedMap.add("properties", updatedBody);
        updatedMap.add("files", file);
        HttpEntity<Object> updatedJson = getHttpEntity(updatedMap, "abcdefg", MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        ResponseEntity<ThreeDModelResponse> updatedResponse = template.exchange("/v1/product/model3d/" + modelId, org.springframework.http.HttpMethod.PUT, updatedJson, ThreeDModelResponse.class);
        assertEquals(200, updatedResponse.getStatusCode().value());
        assertEquals("updated-barcode", updatedResponse.getBody().getBarcode());
    }

    @Test
    void update3DModelWithUnauthorizedRole() {
        Long modelId = 24L;
        String updatedBody = createThreeDModelRequestBody().put("barcode", "updated-barcode").toString();
        MultiValueMap<String, Object> updatedMap = new LinkedMultiValueMap<>();
        updatedMap.add("properties", updatedBody);
        updatedMap.add("files", file);
        HttpEntity<Object> updatedJson = getHttpEntity(updatedMap, "hijkllm", MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        ResponseEntity<ThreeDModelResponse> updatedResponse = template.exchange("/v1/product/model3d/" + modelId, org.springframework.http.HttpMethod.PUT, updatedJson, ThreeDModelResponse.class);
        assertEquals(403, updatedResponse.getStatusCode().value());
    }

    @Test
    void update3DModelWithExistingBarcode() {
        Long modelId = 24L;
        String updatedBody = createThreeDModelRequestBody().put("barcode", "test-barcode2").toString();
        MultiValueMap<String, Object> updatedMap = new LinkedMultiValueMap<>();
        updatedMap.add("properties", updatedBody);
        updatedMap.add("files", file);
        HttpEntity<Object> updatedJson = getHttpEntity(updatedMap, "abcdefg", MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        ResponseEntity<ThreeDModelResponse> updatedResponse = template.exchange("/v1/product/model3d/" + modelId, org.springframework.http.HttpMethod.PUT, updatedJson, ThreeDModelResponse.class);
        assertEquals(409, updatedResponse.getStatusCode().value());
    }

    @Test
    void update3DModelWithExistingSku() {
        Long modelId = 24L;
        String updatedBody = createThreeDModelRequestBody().put("sku", "sku-test2").toString();
        MultiValueMap<String, Object> updatedMap = new LinkedMultiValueMap<>();
        updatedMap.add("properties", updatedBody);
        updatedMap.add("files", file);
        HttpEntity<Object> updatedJson = getHttpEntity(updatedMap, "abcdefg", MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        ResponseEntity<ThreeDModelResponse> updatedResponse = template.exchange("/v1/product/model3d/" + modelId, org.springframework.http.HttpMethod.PUT, updatedJson, ThreeDModelResponse.class);
        assertEquals(409, updatedResponse.getStatusCode().value());
    }

    @Test
    void delete3DModel() {
        Long modelId = 24L;
        HttpEntity<Object> request = getHttpEntity("abcdefg");
        ResponseEntity<String> response = template.exchange("/v1/product/model3d/" + modelId, org.springframework.http.HttpMethod.DELETE, request, String.class);
        assertEquals(200, response.getStatusCode().value());
    }

}
