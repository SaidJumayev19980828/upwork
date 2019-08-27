import java.io.IOException;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.Date;
import java.util.LinkedList;

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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
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
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD , scripts = {"/sql/Payment_Test_Data_Delete.sql"})
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
		order.setCreationDate(new Date());
		order.setUpdateDate(new Date());
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
		order.setBasketsEntity(basketEntity);
				
		
		orderEntity = orderRepository.save(order);
		return orderEntity.getId();
	}
}
