package com.nasnav.yeshtery.test.controllers.yeshtery;

import com.nasnav.dao.ImageTypeRepository;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.persistence.ImageType;
import com.nasnav.response.ImageTypeResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;

import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.concurrent.NotThreadSafe;


import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
@NotThreadSafe
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Images_Types_Test.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,scripts = {"/sql/database_cleanup.sql"})
public class ImageTypesTest extends AbstractTestWithTempBaseDir {

    @Autowired private TestRestTemplate template;
    @Autowired private ImageTypeRepository imageTypeRepository;

    private JSONObject createImageTypeJson(Long id ,Long orgId,String text,String label) {
        JSONObject body = new JSONObject();
        body.put("type_id",id);
        body.put("organization_id", orgId);
        body.put("text",text);
        body.put("label", label);
        return body;
    }

    @Test
    @Order(5)
    void createImageType(){
        JSONObject requestBody = createImageTypeJson(68L,99001L,"test text","test label");
        HttpEntity<Object> request = getHttpEntity(requestBody.toString(),"abcdefg");
        ResponseEntity<ImageTypeResponse> response = template
                .postForEntity("/v1/organization/add/image/type", request, ImageTypeResponse.class);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    @Order(2)
    void updateImageType(){
        JSONObject requestBody=null;
       var imageType= imageTypeRepository.findById(67L);
       if (imageType!=null){
           requestBody = createImageTypeJson(imageType.get().getType_id(), imageType.get().getOrganizationId(), "updated text","updated label");
       }
        HttpEntity<Object> request = getHttpEntity(requestBody.toString(),"abcdefg");
        ResponseEntity response = template
                .postForEntity("/v1/organization/update/image/type", request, void.class);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    @Order(3)
    void getOrganizationImagesTypes() {
        HttpEntity<Object> request = getHttpEntity("abcdefg");
        long orgId = 99001;
        ResponseEntity<String> response = template.exchange("/v1/organization/images/types?org_id=" + orgId ,GET,request, String.class);
        Assert.assertEquals(OK, response.getStatusCode());
    }
    @Test
    @Order(4)
    void deleteImageType(){
        HttpEntity<Object> request = getHttpEntity("abcdefg");
        long typeId = 67L;
        ResponseEntity<String> response = template.exchange("/v1/organization/delete/image/type?type_id=" + typeId ,
                DELETE, request, String.class);
        Assert.assertEquals(OK, response.getStatusCode());

    }
}