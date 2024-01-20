package com.nasnav.yeshtery.controller.v1;

import com.nasnav.response.UserApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
public class TokenHelper {

    private static final WebTestClient WEB_TEST_CLIENT = WebTestClient
            .bindToServer(new ReactorClientHttpConnector())
            .build();

    private static final String token_body = """
            {
              "email": "mohamedghazi.pvt@gmail.com",
              "employee": false,
              "notification_token": "YYYYYYYYYY:XXXXXXXXXXXX",
              "org_id": 6,
              "password": "password",
              "remember_me": true
            }\
            """;

    static String token() {
        // Perform POST request !
        UserApiResponse apiResponse = WEB_TEST_CLIENT.post()
                .uri("http://localhost:8050/v1/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(token_body))
                .exchange()
                .expectBody(UserApiResponse.class)
                .returnResult()
                .getResponseBody();

        log.info("Generated token is: " + apiResponse.getToken());

        return apiResponse.getToken();
    }
}
