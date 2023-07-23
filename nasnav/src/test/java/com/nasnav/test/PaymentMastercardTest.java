package com.nasnav.test;

import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Transactional
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED), scripts={"/sql/database_cleanup.sql"})
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED),  scripts={"/sql/Order_Info_Test.sql"})
@Slf4j
public class PaymentMastercardTest extends AbstractTestWithTempBaseDir {

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
        log.debug(page.asXml());

    }

}
