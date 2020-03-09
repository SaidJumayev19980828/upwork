import java.io.IOException;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nasnav.payments.upg.UpgLightbox;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.NavBox;
import com.nasnav.controller.PaymentControllerMastercard;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.StocksEntity;

import net.jcip.annotations.NotThreadSafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Transactional
@PropertySource("classpath:database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PaymentUpgTest {

	@Mock
	private PaymentControllerMastercard paymentController;

	@LocalServerPort
	int randomServerPort;

	@Autowired
	private OrdersRepository orderRepository;

	@Autowired
	private BasketRepository basketRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	OrganizationRepository orgRepo;
	

	final static LinkedList<WebWindow> windows = new LinkedList<WebWindow>();

	@Before
	public void setup() {
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("merchant.testqnbaatest001", "password".toCharArray());
			}
		});
	}

	@Ignore
	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Payment_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testUpgLightbox() throws FailingHttpStatusCodeException, IOException {

		Long orderId = 1L;

		WebClient webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);


		CookieManager cookieMan = webClient.getCookieManager();
		cookieMan.setCookiesEnabled(false);
		System.out.println("Order: " + orderId);
		HtmlPage page = webClient
				.getPage("http://localhost:" + randomServerPort + "/payment/misr/test/upglightbox?order_id=5&" + orderId);
		assertTrue(page.asText().contains("PAY NOW"));
		assertTrue(page.getWebResponse().getContentAsString().contains("callPaySky"));
		assertTrue(page.getWebResponse().getContentAsString().contains("SecureHash"));
		HtmlButton payNow = (HtmlButton)page.getByXPath("//button").get(0);
		page = payNow.click();
		assertTrue(page.getWebResponse().getContentAsString().contains("Merchant Ref #"));
		assertTrue(page.getWebResponse().getContentAsString().contains("Quick Payment Form"));
		((HtmlInput)page.getElementById("CardNumber")).setValueAttribute("5078036254831639");
		((HtmlInput)page.getElementById("Expiration")).setValueAttribute("09/23");
		((HtmlInput)page.getElementById("CVV")).setValueAttribute("544");
		((HtmlInput)page.getElementById("NameOnCard")).setValueAttribute("Test Owner");
		page = (page.getElementById("pay")).click();

		System.out.println(page.getWebResponse().getContentAsString());

		webClient.close();
	}

	@Test
	public void testHmac() {
		JSONObject result = new JSONObject();
		result.put("DateTimeLocalTrxn", "180829144425");
//		result.put("Amount", 100);
		result.put("MerchantId", "11000000025");
		result.put("TerminalId", 800022);
//		result.put("TrxDateTime", dateFormat.format(now));
//		result.put("MerchantReference", order.getId() + "-" + now.getTime());
//		result.put("SecureHash", calculateHash(result, account.getUpgSecureKey()));
		String hash = UpgLightbox.calculateHash(result, "66623430313531632D663137362D346664332D616634392D396531633665336337376230");
		assertEquals("55d537dbcd8c6cf390cc11e1c2e3452a8f73a7a15462a531fa71baa443254677".toUpperCase(), hash.toUpperCase());
//		System.out.println(hash);

		JSONObject response = new JSONObject();
		response.put("TxnDate", "191030143939");
//		response.put("SystemReference", "49374");
//		response.put("NetworkReference", "930314918143");
//		response.put("MerchantReference", "5-1572439135294");
		response.put("Amount", "13500");
		response.put("Currency", "818");
		response.put("PaidThrough", "Card");
//		response.put("PayerName", "Marek");
//		response.put("PayerAccount", "507803XXXXXX1639");
//		response.put("ProviderSchemeName", "");
		response.put("MerchantId", "10000001117");
		response.put("TerminalId", 100083);
		String hash2 = UpgLightbox.calculateHash(response, "66623430313531632D663137362D346664332D616634392D396531633665336337376230");
		assertEquals("C95C35D54BD0C9BDF6FFB6008F9CF71B000754146C4C693B2B9CBD0EF021D410".toUpperCase(), hash2.toUpperCase());
	}


}
