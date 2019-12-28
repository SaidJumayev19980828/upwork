import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;

import com.nasnav.payments.qnb.QnbSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.nasnav.NavBox;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.qnb.QnbAccount;
import com.nasnav.payments.mastercard.Session;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Qnb_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class QnbHostedSessionPayment {

    @Autowired
    private QnbSession session;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ShopsRepository shopsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;
    private ShopsEntity shop;
    private OrganizationEntity org;
    private UserEntity user;
    private Long orderId;
    
    
    @Autowired
    private ProductVariantsRepository productVariantRepository;


    @Before
    public void setup(){
        orderId = createOrder();
        ((QnbAccount)session.getMerchantAccount()).setup();
    }


    @Test
    public void rawSessionCreationTest() throws BusinessException {
//        session.setMerchantAccount(testAccount);
        Assert.assertNotNull(session.initialize(orderRepository.findById(orderId).get()));
    }

    @Test
    public void initSessionViaApiTest() {
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
                    .returnResult().getResponseBody();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Long createOrder() {

        //get organization
        org = organizationRepository.findOneById(99001L);
        
        
        //create product
        ProductEntity product = new ProductEntity();
        product.setName("product one");
        product.setOrganizationId(org.getId());
        ProductEntity productEntity = productRepository.save(product);
        
        //create base variant
        ProductVariantsEntity variant = new ProductVariantsEntity();
        variant.setFeatureSpec("{}");
        variant.setProductEntity(productEntity);
        variant.setName("variant name");
        variant = productVariantRepository.save(variant);
        
        //create shop
        shop = new ShopsEntity();
        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
        shop.setOrganizationEntity(org);
        ShopsEntity shopEntity = shopsRepository.save(shop);

        //create stock
        StocksEntity stock = new StocksEntity();
        stock.setPrice(new BigDecimal(100));
        stock.setProductVariantsEntity(variant);
        stock.setQuantity(5);
        stock.setCurrency(TransactionCurrency.EGP);
        stock.setShopsEntity(shopEntity);
        stock.setOrganizationEntity(org);
        StocksEntity stockEntity = stockRepository.save(stock);
        
        //create user
        user = new UserEntity();
        user.setName("John smith");
        user.setEmail("bi@Oooooo.com");
        user.setEncryptedPassword("");
        user.setOrganizationId(org.getId());
        userRepository.save(user);
        
        // create order
        OrdersEntity order = new OrdersEntity();
        
//        order.setCreationDate(new Date());
        order.setUpdateDate( LocalDateTime.now() );
        order.setAmount(new BigDecimal(500));
        order.setShopsEntity(shopEntity);
        order.setEmail("test@nasnav.com");
        order.setOrganizationEntity(org);
        order.setUserId(user.getId());
        OrdersEntity orderEntity = orderRepository.save(order);

        BasketsEntity basket = new BasketsEntity();
        basket.setOrdersEntity(orderEntity);
        basket.setQuantity(new BigDecimal(5));
        basket.setStocksEntity(stockEntity);
        basket.setPrice(stockEntity.getPrice());
        basket = basketRepository.save(basket);
        HashSet<BasketsEntity> baskets = new HashSet<BasketsEntity>();
        baskets.add(basket);
        orderEntity.setBasketsEntity(baskets);
        orderEntity = orderRepository.save(order);
        return orderEntity.getId();
    }
}
