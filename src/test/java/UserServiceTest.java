import com.nasnav.NavBox;
import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.UserEntity;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class UserServiceTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void methodTest() {
        this.webClient.get().uri(TestConfig.BaseURL + "/user/register").exchange().expectStatus().isEqualTo(405);
    }

    @Test
    public void missingDataTest() {
        WebTestClient.ResponseSpec response = webClient.post().uri(TestConfig.BaseURL + "/user/register").attribute("name", "Michal").exchange();
        response.expectStatus().isOk();
        response.expectBody()
                .jsonPath("$.success").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    public void simpleUserCreationTest() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "John Smith");
        formData.add("email", "testemail@nasnav.com");

        WebTestClient.ResponseSpec response = webClient.post().uri(TestConfig.BaseURL + "/user/register")
                .body(BodyInserters.fromFormData(formData)).exchange();

        JSONObject jsonResponse = new JSONObject(
                new String(
                    response
                        .expectStatus().isOk()
                        .expectBody()
                            .jsonPath("$.success").isNotEmpty()
                            .jsonPath("$.success").isEqualTo(true)
                    .returnResult().getResponseBody()
                )
        );

        // if succesful, clean up the database by removing the record created
        long createdUserId = jsonResponse.getInt("id");
        UserEntity ue = userRepository.findById(createdUserId).get();
        userRepository.delete(ue);
    }
}