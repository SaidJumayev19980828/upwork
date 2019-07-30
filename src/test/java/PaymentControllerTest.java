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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nasnav.NavBox;
import com.nasnav.controller.QnbPaymentController;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.payments.qnb.PaymentService;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.ProductEntity;
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
	private TestRestTemplate template;

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
	private ProductRepository productRepository;

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
	public void testCompletePaymentRedirection() throws FailingHttpStatusCodeException, MalformedURLException, IOException {

		Long orderId = createOrder();
		BigDecimal orderValue = getOrderValue(orderId);
		// ... set up other values, like items etc.

		String url = "/payment/qnb/test/payment/init";
		WebClient webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);

/*
		webClient.addWebWindowListener(new WebWindowListener() {
			public void webWindowClosed(WebWindowEvent event) {
			}

			public void webWindowContentChanged(WebWindowEvent event) {
				windows.add(event.getWebWindow());
			}

			public void webWindowOpened(WebWindowEvent event) {
				windows.add(event.getWebWindow());
			}
		});

*/
		CookieManager cookieMan = webClient.getCookieManager();
		cookieMan.setCookiesEnabled(false);

		HtmlPage page = webClient
				.getPage("http://localhost:" + randomServerPort + "/payment/qnb/test/payment/init?order_id=" + orderId);
/*
		// extract the part of JavaScript that configures the payment processing library
		Matcher jsConfigMatcher = Pattern.compile("Checkout.configure\\([^\\)]+").matcher(page.asXml());
		Assert.assertTrue(jsConfigMatcher.find());
		String jsConfig = jsConfigMatcher.group();
		System.out.println(jsConfig);
		Assert.assertTrue(Pattern.compile("amount\\s+:\\s+." + orderValue).matcher(jsConfig).find());
		Assert.assertTrue(Pattern.compile("id: .SESSION[0-9A-Z]{10,}").matcher(jsConfig).find());
		// check for other values ....
System.out.println(page.asXml());
		HtmlButtonInput paymentPageButton = (HtmlButtonInput) page.getByXPath("//input[@type='button']").get(1);
		paymentPageButton.click();

		HtmlPage fullQNBHtmlPage = getPopupPage();
		Assert.assertTrue(fullQNBHtmlPage.asText().contains("Hosted Checkout"));
*/

		webClient.close();
	}

	private static HtmlPage getPopupPage() {
		WebWindow latestWindow = windows.getLast();
		return (HtmlPage) latestWindow.getEnclosedPage();
	}

	private void addOrderAndBasketToDB() {
		//TODO add the necesary params to signature
	}

	private BigDecimal getOrderValue(Long orderId) {
		return basketRepository.findByOrdersEntity_Id(orderId).stream().map(BasketsEntity::getPrice).reduce(BigDecimal::add).get();
	}

	private Long createOrder() {
		//create product
		ProductEntity product = new ProductEntity();
		product.setName("product one");
		product.setCreationDate(new Date());
		product.setUpdateDate(new Date());
		ProductEntity productEntity = productRepository.save(product);
		//create stock
		StocksEntity stock = new StocksEntity();
		stock.setPrice(new BigDecimal(100));
		stock.setCreationDate(new Date());
		stock.setUpdateDate(new Date());
		stock.setProductEntity(productEntity);
		StocksEntity stockEntity = stockRepository.save(stock);

		// create order
		OrdersEntity order = new OrdersEntity();
//		order.setCreationDate(new Date());
		order.setUpdateDate(new Date());
		order.setAmount(new BigDecimal(50));
		order.setEmail("test@nasnav.com");
		OrdersEntity orderEntity = orderRepository.save(order);
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