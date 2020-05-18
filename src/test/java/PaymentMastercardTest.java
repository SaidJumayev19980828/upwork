import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nasnav.NavBox;

import com.nasnav.dao.OrdersRepository;
import com.nasnav.persistence.OrdersEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.jcip.annotations.NotThreadSafe;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Transactional
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED), scripts={"/sql/database_cleanup.sql"})
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED),  scripts={"/sql/Order_Info_Test.sql"})
public class PaymentMastercardTest {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    Environment environment;

    @Autowired
    private DataSource dataSource;

    @Ignore
    @Test
    public void testLightboxPaymentWithCreditCard() throws Exception {


        String url="http://localhost:" + environment.getProperty("local.server.port");
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.104 Safari/537.36)");
        webClient.addRequestHeader("Accept-Language","en-US,en;q=0.8,pl;q=0.6");
        webClient.getCookieManager().setCookiesEnabled(true);

        // try to pay for orders of different users
        UnexpectedPage errpage = webClient.getPage(url + "/payment/mcard/qnb/test/lightbox?order_id=330002,330006");
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), errpage.getWebResponse().getStatusCode());

        HtmlPage page = webClient.getPage(url + "/payment/mcard/qnb/test/lightbox?order_id=330002,330003");
        assertEquals(HttpStatus.OK.value(), page.getWebResponse().getStatusCode());
        assertTrue(page.asXml().contains("\"order_amount\":10500.0000"));
        HtmlButtonInput payButton = (HtmlButtonInput)(page.getByXPath("//input[@type='button']").get(0));
        page = payButton.click();
        System.out.println(page.asXml());

    }

}
