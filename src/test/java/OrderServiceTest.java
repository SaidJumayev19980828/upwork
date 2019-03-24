import com.nasnav.NavBox;
import com.nasnav.dao.OrdersRepository;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class OrderServiceTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrdersRepository orderRepository;

    @Test
    public void methodTest() {
        this.webClient.get().uri(TestConfig.BaseURL + "/order/update").exchange().expectStatus().isEqualTo(200);
    }
}