import com.nasnav.NavBox;
import com.nasnav.dao.*;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.qnb.Account;
import com.nasnav.payments.qnb.Session;
import com.nasnav.persistence.*;
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
import java.util.*;

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
    private ShopsRepository shopsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    public void rawSessionCreationTest() throws BusinessException {
        session.setMerchantAccount(testAccount);
        Long orderId = createOrder();
        Assert.assertNotNull(session.initialize(orderRepository.findById(orderId).get()));

        //delete baskets
        List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(orderId);
        for(BasketsEntity basket : baskets){
            basketRepository.delete(basket);
            stockRepository.delete(basket.getStocksEntity());
            productRepository.delete(basket.getStocksEntity().getProductEntity());
        }
        //delete payment
        paymentsRepository.deleteById(paymentsRepository.findByUid(session.getOrderRef()).get().getId());
        //delete created order
        orderRepository.deleteById(orderId);
    }

    @Test
    public void initSessionViaApiTest() {
        Long orderId = createOrder();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("order_id", orderId.toString());

        try {
            WebTestClient.ResponseSpec response = webClient.post().uri("/payment/qnb/initialize")
                    .body(BodyInserters.fromFormData(formData)).exchange();
            response.expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isNotEmpty()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.order_uid").isEqualTo(session.getOrderRef())
                    .jsonPath("$.order_amount").isEqualTo(500.0)
                    .jsonPath("$.order_currency").isEqualTo(TransactionCurrency.EGP.name())
                    .jsonPath("$.session_id").isEqualTo(session.getSessionId())
//                    .jsonPath("$.basket").isNotEmpty()
//                    .jsonPath("$.basket[0].quantity").isEqualTo(5)
//                    .jsonPath("$.basket.size()").isEqualTo(1)
                    .returnResult().getResponseBody();
        } catch(Exception e) {
            e.printStackTrace();
        }

        //delete baskets
        List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(orderId);
        for(BasketsEntity basket : baskets){
            basketRepository.delete(basket);
            stockRepository.delete(basket.getStocksEntity());
            productRepository.delete(basket.getStocksEntity().getProductEntity());
        }
        //delete payment
        paymentsRepository.deleteById(paymentsRepository.findByUid(session.getOrderRef()).get().getId());
        //delete created order
        orderRepository.deleteById(orderId);
    }

    private Long createOrder() {

        //create organization
        OrganizationEntity org = new OrganizationEntity();
        org.setName("organization");
        org.setDescription("org descr");
        org.setCreatedAt(new Date());
        org.setUpdatedAt(new Date());
        org = organizationRepository.save(org);
        //create product
        ProductEntity product = new ProductEntity();
        product.setName("product one");
        product.setCreationDate(new Date());
        product.setUpdateDate(new Date());
        ProductEntity productEntity = productRepository.save(product);

        //create shop
        ShopsEntity shop = new ShopsEntity();
        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
        ShopsEntity shopEntity = shopsRepository.save(shop);

        //create stock
        StocksEntity stock = new StocksEntity();
        stock.setPrice(new BigDecimal(100));
        stock.setCreationDate(new Date());
        stock.setUpdateDate(new Date());
        stock.setProductEntity(productEntity);
        stock.setQuantity(5);
        stock.setCurrency(TransactionCurrency.EGP);
        stock.setShopsEntity(shopEntity);
        StocksEntity stockEntity = stockRepository.save(stock);

        // create order
        OrdersEntity order = new OrdersEntity();
//        order.setCreationDate(new Date());
        order.setUpdateDate(new Date());
        order.setAmount(new BigDecimal(500));
        order.setShopsEntity(shopEntity);
        order.setEmail("test@nasnav.com");
        order.setOrganizationEntity(org);
        OrdersEntity orderEntity = orderRepository.save(order);

        BasketsEntity basket = new BasketsEntity();
        basket.setOrdersEntity(orderEntity);
        basket.setQuantity(new BigDecimal(5));
        basket.setStocksEntity(stockEntity);
        basket.setPrice(stockEntity.getPrice());
        basket = basketRepository.save(basket);
        orderEntity.setBasketsEntity(basket);
        orderEntity = orderRepository.save(order);
        return orderEntity.getId();
    }
}
