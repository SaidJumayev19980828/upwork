package com.nasnav.yeshtery.test.controllers;



import com.nasnav.commons.YeshteryConstants;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ActiveProfiles("test")
public class SettingsControllerTest extends AbstractTestWithTempBaseDir {
    private static final String YESHTERY_FRONTEND_SETTING_API_PATH = YeshteryConstants.API_PATH + "/frontend/setting";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void frontEndSettingsTest_200() {
        ResponseEntity<Object> forEntity = testRestTemplate
                .getForEntity(YESHTERY_FRONTEND_SETTING_API_PATH+"?frontend_id=dash2", Object.class);
        assertEquals(HttpStatus.OK, forEntity.getStatusCode());
        Map<String,Object> body = (Map<String, Object>) forEntity.getBody();
        String firstKeyFromResponse = getFirstKeyFromResponse(body);
        String firstKeyFromJson = getFirstKeyFromJson();
        assertEquals(body.size(),1);
        assertEquals(firstKeyFromResponse,firstKeyFromJson);
        String firstValueFromResponse = getFirstValueFromResponse(body);
        String firstValueFromJosn = getFirstValueFromJosn();
        assertEquals(firstValueFromJosn,firstValueFromResponse);
    }

    private JSONObject expectedResponse() {
        JSONObject jsonRes = new JSONObject();
        jsonRes.put("firebase_api_key", "${nasnav.frontend.dash2.firebase_api_key}");
        return jsonRes;
    }

    private String getFirstKeyFromJson() {
        JSONObject jsonObject = expectedResponse();
      return  jsonObject.keySet()
                .stream()
                .findFirst()
                .get();
    }

    private String getFirstValueFromJosn() {
        JSONObject jsonObject = expectedResponse();
      return (String) jsonObject
                        .toMap()
                        .values()
                        .stream()
                        .findFirst()
                        .get();
    }

    private String getFirstKeyFromResponse(Map<String, Object> body) {
       return    body.keySet()
                    .stream()
                      .findFirst()
                       .get();
    }

    private String getFirstValueFromResponse(Map<String, Object> body) {
        return   (String) body.values()
                .stream()
                .findFirst()
                .get();
    }

}


