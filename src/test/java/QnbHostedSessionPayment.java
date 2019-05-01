import com.nasnav.NavBox;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.payments.qnb.Account;
import com.nasnav.payments.qnb.Session;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.StocksEntity;
import org.json.JSONObject;
import org.junit.Assert;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class QnbHostedSessionPayment {
    Account testAccount = new Account();

    @Autowired
    private Session session;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void rawSessionCreationTest() {
        session.setMerchantAccount(testAccount);
        Long orderId = createOrder();
        Assert.assertTrue(session.initialize(orderId, Session.TransactionCurrency.EGP));

        //delete baskets
        List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(orderId);
        for(BasketsEntity basket : baskets){
            basketRepository.delete(basket);
            stockRepository.delete(basket.getStocksEntity());
            productRepository.delete(basket.getStocksEntity().getProductEntity());
        }
        //delete created order
        orderRepository.deleteById(orderId);
    }

    @Test
    public void initSessionViaApiTest() {
        Long orderId = createOrder();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("order_id", orderId.toString());

        WebTestClient.ResponseSpec response = webClient.post().uri("/payment/qnb/initialize")
                .body(BodyInserters.fromFormData(formData)).exchange();
        try {
            response.expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isNotEmpty()
                    .jsonPath("$.success").isEqualTo(true)
                    .returnResult().getResponseBody();
        }catch(Exception e){
            e.printStackTrace();
        }

        //delete baskets
        List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(orderId);
        for(BasketsEntity basket : baskets){
            basketRepository.delete(basket);
            stockRepository.delete(basket.getStocksEntity());
            productRepository.delete(basket.getStocksEntity().getProductEntity());
        }
        //delete created order
        orderRepository.deleteById(orderId);
    }

    private Long createOrder() {
        //create product
        ProductEntity product = new ProductEntity();
        product.setName("product one");
        product.setCreationdDate(new Date());
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
        order.setCreationDate(new Date());
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
