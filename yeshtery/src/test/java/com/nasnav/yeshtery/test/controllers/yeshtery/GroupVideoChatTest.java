package com.nasnav.yeshtery.test.controllers.yeshtery;


import com.nasnav.dto.response.ChatWidgetSettingResponse;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import com.nasnav.service.impl.VideoChatServiceImpl;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.SessionProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "/sql/Video_chat_test_data.sql" })
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
@Slf4j
public class GroupVideoChatTest extends AbstractTestWithTempBaseDir {

    private RestTemplate restTemplate;

    @Mock
    private OpenVidu openVidu;

    @MockBean
    private VideoChatService videoChatService;

    @SneakyThrows
    @BeforeEach
    public void init()  {
        videoChatService = mock(VideoChatServiceImpl.class);
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        restTemplate = new RestTemplate(factory);

        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token","aasdas");
                    return execution.execute(request, body);
                }));
        lenient()
                .when(videoChatService.createGroupVideoChat(anyString(), anyLong(), anyLong()))
                        .thenReturn(new VideoChatResponse());
        lenient()
                .when(videoChatService.getGroupVideoChat(anyString(), anyLong()))
                .thenReturn(new VideoChatResponse());
        lenient()
                .when(openVidu.createSession(any(SessionProperties.class)))
                .then(invocationOnMock -> "testSession");

    }
    @Autowired
    private TestRestTemplate template;

    @Test
    public void testCreateGroupSession() {
        HttpEntity<?> request = getHttpEntity("131415");
        ResponseEntity<ChatWidgetSettingResponse> result =
                template.exchange("/videochat/group/session/create?org_id=99001&session_name=testsession&shop_id=502", POST, request, ChatWidgetSettingResponse.class);

        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void testjoinGroupSession() {
        HttpEntity<?> request = getHttpEntity("131415");
        ResponseEntity<ChatWidgetSettingResponse> result =
                template.exchange("/videochat/group/session/get?org_id=99001&session_name=testsession", GET, request, ChatWidgetSettingResponse.class);

        assertEquals(200, result.getStatusCodeValue());
    }

}
