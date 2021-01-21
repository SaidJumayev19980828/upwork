package com.nasnav.test;
import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;
import com.nasnav.dao.*;
import com.nasnav.dto.CountriesRepObj;
import com.nasnav.persistence.SocialEntity;
import com.nasnav.persistence.SubAreasEntity;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.shipping.services.DummyShippingService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert.sql","/sql/Extra_Features_Data_Insert.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class OrganizationManagmentTest {
    @Value("classpath:sql/database_cleanup.sql")
    private Resource databaseCleanup;
    @Value("classpath:test_imgs_to_upload/nasnav--Test_Photo.png")
    private Resource file;
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ExtraAttributesRepository extraAttrRepo;
    @Autowired
    private ProductExtraAttributesEntityRepository productExtraAttrRepo;
    @Autowired
    private SocialRepository socialRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubAreaRepository subAreaRepo;

    @Test
    public void updateOrganizationDataSuccessTest() {
        String facebookUrl = "https://www.facebook.com/fortune.stores11/";
        String twitterUrl = "https://www.twitter.com/fortunestores/";
        String instagramUrl = "https://www.instagram.com/islamify/";
        String youtubeUrl = "https://www.youtube.com/";
        String linkedinUrl = "https://www.linkedin.com/";
        String pinterestUrl = "https://www.pinterest.com/";
        String body = json()
                .put("description", "this company is old and unique")
                .put("social_twitter", twitterUrl)
                .put("social_facebook", facebookUrl)
                .put("social_instagram",instagramUrl)
                .put("social_youtube", youtubeUrl)
                .put("social_linkedin", linkedinUrl)
                .put("social_pinterest", pinterestUrl)
                .toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(99001L).get();
        assertEquals(twitterUrl, socialEntity.getTwitter());
        assertEquals(facebookUrl, socialEntity.getFacebook());
        assertEquals(instagramUrl, socialEntity.getInstagram());
        assertEquals(youtubeUrl, socialEntity.getYoutube());
        assertEquals(linkedinUrl, socialEntity.getLinkedin());
        assertEquals(pinterestUrl, socialEntity.getPinterest());
    }




    //trying to update organization with nasnav_admin user
    @Test
    public void updateOrganizationUnauthorizedUserTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is o8895ssffld and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(403, response.getStatusCode().value());
    }



    @Test
    public void updateOrganizationInvalidSocialLinksTest() {
        // invalid twitter link
        String body = "{\"org_id\":99005, \"social_twitter\": \"htps://www.twitte.com/fortunestores\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());

        // invalid facebook link
        body = "{\"org_id\":99005, \"social_facebook\": \"htts://www.faceboo.com/fortune.stores11/\"}";
        map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());

        // invalid instagram link
        body = "{\"org_id\":99005, \"social_instagram\": \"htps://instgram.com/fortunestores\"}";
        map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrganizationInvalidLogoTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is old and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", databaseCleanup);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }


    @Test
    public void createOrganizationSuccessTest() {
        String body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad-pant-trello\"}";
        HttpEntity<Object> json = getHttpEntity(body,"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        organizationRepository.deleteById(response.getBody().getOrganizationId());
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void createOrganizationMissingValuesTest() {
        String body = "{\"p_name\":\"solad-pant\"}";
        HttpEntity<Object> json = getHttpEntity(body, "abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());

        body = "{\"name\":\"Solad Pant\"}";
        json = getHttpEntity(body,"abcdefg");
        response = template.postForEntity("/admin/organization", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }


    @Test
    public void createOrganizationInvalidValuesTest() {
        String body = "{\"name\":\"23Solad Pant#\", \"p_name\":\"solad-pant\"}";
        HttpEntity<Object> json = getHttpEntity(body,"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());

        body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad_pant#$!^*\"}";
        json = getHttpEntity(body,"abcdefg");
        response = template.postForEntity("/admin/organization", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    //trying to create organization with organization_admin user
    @Test
    public void createOrganizationUnauthorizedUserTest() {
        String body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad-pant\"}";
        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        assertEquals(403, response.getStatusCode().value());
    }


    @Test
    public void getOrgByURLTestSuccess() throws URISyntaxException {
        URI url = new URI("http://www.fortune.nasnav.com/");
        ResponseEntity<String> response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);

        url = new URI("https://www.nasnav.com/fortune/product/74");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);

        url = new URI("www.fortune-egypt.com/categories/");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);
    }

    @Test
    public void getOrgByURLNoOrgTest() throws URISyntaxException {
        URI url = new URI("http://www.invaliddomain.nasnav.com/");
        ResponseEntity<String> response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);

        url = new URI("https://www.nasnav.com/invaliddomain/product/74");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);

        url = new URI("https://www.invaliddomain-egypt.com/categories/");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);
    }


    @Test
    public void updateOrgNameTest () {
        JSONObject body = json().put("id", 99001)
                                .put("name", "new org name")
                                .put("p_name", "org-name");
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals("new org name", org.getName());
        assertEquals("org-name", org.getPname());
    }


    @Test
    public void updateOrgEcommerceTest () {
        JSONObject body = json().put("id", 99001)
                .put("ecommerce", 1);
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals(1, org.getEcommerce().intValue());
    }


    @Test
    public void updateOrgGoogleTokenTest () {
        JSONObject body = json().put("id", 99001)
                .put("google_token", "tokeee-eeee-eeeee-eeen");
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals("tokeee-eeee-eeeee-eeen", org.getGoogleToken());
    }


    @Test
    public void updateOrgCurrencyIsoTest () {
        JSONObject body = json().put("id", 99001)
                .put("currency_iso", 818);
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals(818, org.getCountry().getIsoCode().intValue());
    }


    @Test
    public void deleteVariantExtraAttribute() {
        //deleting extra attribute which not attached to any variant
        HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange("/organization/extra_attribute?attr_id=11002",
                DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue(!extraAttrRepo.existsByIdAndOrganizationId(11001, 99002L));
    }


    @Test
    public void deleteVariantExtraAttributeNonExistInSameOrg() {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = template.exchange("/organization/extra_attribute?attr_id=11001",
                DELETE, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }


    @Test
    public void deleteVariantExtraAttributeNoAuthZ() {
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res = template.exchange("/organization/extra_attribute?attr_id=11001",
                DELETE, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    @Test
    public void deleteVariantExtraAttributeNoAuthN() {
        HttpEntity<?> req = getHttpEntity("noneexist");
        ResponseEntity<String> res = template.exchange("/organization/extra_attribute?attr_id=11002",
                DELETE, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }


    @Test
    public void deleteVariantExtraAttributeAttachedVariant() {
    	assertTrue(extraAttrRepo.existsByIdAndOrganizationId(11003, 99002L));
        assertTrue(productExtraAttrRepo.existsById(11003L));
        
        //deleting extra attribute attached to variant #310002
        HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange("/organization/extra_attribute?attr_id=11003",
                DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue(!extraAttrRepo.existsByIdAndOrganizationId(11003, 99002L));
        assertTrue(!productExtraAttrRepo.existsById(11003L));
    }
    
    
    
    @Test
    public void deleteVariantExtraAttributeOfDeletedProduct() {
    	assertTrue(extraAttrRepo.existsByIdAndOrganizationId(11004, 99002L));
        assertTrue(productExtraAttrRepo.existsById(11004L));
        
        //deleting extra attribute attached to variant #310002
        HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange("/organization/extra_attribute?attr_id=11004",
                DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue(!extraAttrRepo.existsByIdAndOrganizationId(11004, 99002L));
        assertTrue(!productExtraAttrRepo.existsById(11004L));
    }


    private void checkSuccessResponse(ResponseEntity<String> response) {
        JSONObject json = new JSONObject(response.getBody());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(99001, json.getInt("id"));
    }

    private void checkFailResponse(ResponseEntity<String> response) {
        JSONObject json = new JSONObject(response.getBody());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, json.getInt("id"));
    }
    
    
    
    
    @Test
    public void getOrganizationShippingServiceNoAuthZ() {
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shipping/service", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    
    @Test
    public void getOrganizationShippingServiceNoAuthN() {
        HttpEntity<?> req = getHttpEntity("NotExist");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shipping/service", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }
    
    
    
    
    @Test
    public void getOrganizationShippingService() throws Exception {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shipping/service", GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        
        List<ShippingServiceRegistration> services = objectMapper.readValue(res.getBody(), new TypeReference<List<ShippingServiceRegistration>>() {});
        assertEquals(1, services.size());
        assertEquals(DummyShippingService.ID, services.get(0).getServiceId());
        assertFalse(services.get(0).getServiceParameters().isEmpty());
    }
    
    
    
    
    
    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/ExtraAttributes_Test_Data_Insert.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getOrganizationShops() throws Exception {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shops", GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        
        List<ShopRepresentationObject> services = objectMapper.readValue(res.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(2, services.size());
        Set<Long> fetchedIds = 
        		services
        		.stream()
        		.map(ShopRepresentationObject::getId)
        		.collect(toSet());
        assertTrue(fetchedIds.containsAll(asList(100001L, 100003L)));
    }
    
    
    
    
    @Test
    public void getOrganizationShopsNoAuthZ() {
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shops", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    
    @Test
    public void getOrganizationShopsNoAuthN() {
        HttpEntity<?> req = getHttpEntity("NotExist");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shops", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasNoAuthZ(){
        HttpEntity<?> req = getHttpEntity("NotExist");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasNoAuthN(){
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasInvalidArea(){
        String name = "werwerland";
        Long areaId = null;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasMissingArea(){
        String name = "werwerland";
        Long areaId = -1L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasInvalidName(){
        String name = "";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasMissingName(){
        String name = null;
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }





    @Test
    public void addSubAreasSuccess(){
        String name = "Fofo compound";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(200, res.getStatusCodeValue());

        ResponseEntity<String> countriesResponse =
                template.exchange("/navbox/countries?org_id=99001", GET, req, String.class);
        String subAreaSavedName =
                JsonPath.read(countriesResponse.getBody(), format("$['Egypt']['cities']['Cairo']['areas']['new cairo']['sub_areas']['%s']['name']", name));
        assertEquals(name, subAreaSavedName);

        //TODO: test old sub-areas were cleared from addresses
        //TODO: test old sub-areas were deleted
    }




    @Test
    public void updateSubAreasSuccess(){
        Long id = 888001L;
        String name = "werwerland";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(id, name, areaId);

        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(200, res.getStatusCodeValue());

        SubAreasEntity savedSubArea = subAreaRepo.findByIdAndOrganization_Id(id, 99001L).get();
        assertEquals(name, savedSubArea.getName());
    }




    private JSONObject createSubAreaUpdateRequest(String name, Long areaId) {
        return createSubAreaUpdateRequest(null, name, areaId);
    }




    private JSONObject createSubAreaUpdateRequest(Long id, String name, Long areaId) {
        return json()
                .put("sub_areas",
                        jsonArray()
                                .put(json()
                                        .put("id", nullableJsonValue(id))
                                        .put("name", nullableJsonValue(name))
                                        .put("area_id", nullableJsonValue(areaId))
                                )
                );
    }



}