package com.nasnav.yeshtery.test;

import com.nasnav.dto.response.ChatWidgetSettingResponse;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.yeshtery.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@RunWith(SpringRunner.class)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Chat_Widget_Setting_APIs_Test_Insert.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@Slf4j
public class ChatWidgetSettingTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Chat_Widget_Setting_APIs_Test_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void updateUnpublishedSettings() {
        var body = json()
                .put("value", "tested value")
                .put("organizationId", 99001)
                        .toString();
        HttpEntity<?> request =  getHttpEntity(body.toString(),"131415");
        ResponseEntity<ChatWidgetSettingResponse> result =
                template.exchange("/v1/chat-widget-setting/create?", POST, request, ChatWidgetSettingResponse.class);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(Objects.nonNull(result.getBody()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Chat_Widget_Setting_APIs_Test_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void publishChatWidgetSettingsForOrg() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<ChatWidgetSettingResponse>  result = template.exchange("/v1/chat-widget-setting/publish?org_id=99001", POST, request, ChatWidgetSettingResponse.class);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(Objects.nonNull(result.getBody()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Chat_Widget_Setting_APIs_Test_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getPublishedChatWidgetSettingsForOrg() {
        ResponseEntity<ChatWidgetSettingResponse>  result = template.getForEntity("/v1/chat-widget-setting/get-published?org_id=99001", ChatWidgetSettingResponse.class);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(Objects.nonNull(result.getBody()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Chat_Widget_Setting_APIs_Test_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getUnPublishedChatWidgetSettingsForOrg() {
        ResponseEntity<ChatWidgetSettingResponse>  result = template.exchange("/v1/chat-widget-setting/get-unpublished?org_id=99001",GET,null, ChatWidgetSettingResponse.class);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(Objects.nonNull(result.getBody()));
    }



}
