import java.io.IOException;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.time.LocalDateTime;
import java.util.*;

import com.nasnav.payments.qnb.UpgLightbox;
import org.json.JSONObject;
import org.junit.Before;
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

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.nasnav.NavBox;
import com.nasnav.controller.QnbPaymentController;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.payments.qnb.PaymentService;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.StocksEntity;

import net.jcip.annotations.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Transactional
public class PaymentControllerTest {

	@Mock
	private QnbPaymentController paymentController;

	@Autowired
	PaymentService paymentService;

	@LocalServerPort
	int randomServerPort;

	@Autowired
	private OrderRepository orderRepository;

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

	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Payment_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testCompletePaymentRedirection() throws FailingHttpStatusCodeException, MalformedURLException, IOException {

		Long orderId = createOrder();

		WebClient webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);


		CookieManager cookieMan = webClient.getCookieManager();
		cookieMan.setCookiesEnabled(false);

		webClient
				.getPage("http://localhost:" + randomServerPort + "/payment/qnb/test/payment/init?order_id=" + orderId);


		webClient.close();
	}

	private Long createOrder() {
		
		//get dummy  stock		
		StocksEntity stockEntity = stockRepository.findById(601L).get();

		// create order
		OrdersEntity order = new OrdersEntity();
		order.setCreationDate( LocalDateTime.now()  );
		order.setUpdateDate( LocalDateTime.now() );
		order.setAmount(new BigDecimal(50));
		order.setEmail("test@nasnav.com");
		OrdersEntity orderEntity = orderRepository.save(order);
		
		//set organization for the order
		OrganizationEntity organizationEntity = orgRepo.findOneById(99001L);
		order.setOrganizationEntity(organizationEntity);
		
		BasketsEntity basket = new BasketsEntity();
		basket.setCurrency(1);
		basket.setPrice(new BigDecimal(100));
		basket.setQuantity(new BigDecimal(5));
		basket.setStocksEntity(stockEntity);
		basket.setOrdersEntity(orderEntity);
		BasketsEntity basketEntity = basketRepository.save(basket);
		HashSet<BasketsEntity> baskets = new HashSet<BasketsEntity>();
		baskets.add(basket);
		order.setBasketsEntity(baskets);
				
		
		orderEntity = orderRepository.save(order);
		return orderEntity.getId();
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Payment_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void testUpgLightbox() throws FailingHttpStatusCodeException, IOException {

		Long orderId = createOrder();

		WebClient webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);


		CookieManager cookieMan = webClient.getCookieManager();
		cookieMan.setCookiesEnabled(false);

		webClient
				.getPage("http://localhost:" + randomServerPort + "/payment/qnb/test/payment/init?order_id=" + orderId);


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
