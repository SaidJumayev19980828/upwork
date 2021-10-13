package com.nasnav.service;

import com.nasnav.dao.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.helpers.CartServiceHelper;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import com.nasnav.service.sendpulse.SendPulseService;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.MathUtils.nullableBigDecimal;
import static com.nasnav.constatnts.EmailConstants.ABANDONED_CART_TEMPLATE;
import static com.nasnav.enumerations.Settings.ORG_EMAIL;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.persistence.PromotionsEntity.DISCOUNT_AMOUNT;
import static com.nasnav.persistence.PromotionsEntity.DISCOUNT_PERCENT;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class CartServiceImpl implements CartService{

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private SecurityService securityService;
    @Autowired
    private PromotionsService promoService;

    @Autowired
    private CartItemRepository cartItemRepo;
    @Autowired
    private PromotionRepository promotionRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductImageService imgService;
    @Autowired
    private DomainService domainService;

    @Autowired
    private OrderService orderService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private MailService mailService;
    @Autowired
    private OrderEmailServiceHelper orderEmailHelper;
    @Autowired
    private CartServiceHelper cartServiceHelper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    private AppConfig config;

    @Autowired
    private TierServiceImp tierServiceImp;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinsDropService coinsDropService;
    @Autowired
    private MetaOrderRepository metaOrderRepository;
    @Autowired
    private BoosterRepository boosterRepository;

    @Override
    public Cart getCart(String promoCode) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
        return getUserCart(user.getId(), promoCode);
    }

    @Override
    public Cart getUserCart(Long userId) {
        Cart cart = new Cart(toCartItemsDto(cartItemRepo.findCurrentCartItemsByUser_Id(userId)));
        cart.getItems().forEach(cartServiceHelper::replaceProductIdWithGivenProductId);
        cart.getItems().forEach(cartServiceHelper::addProductTypeFromAdditionalData);
        cart.setSubtotal(calculateCartTotal(cart));
        return cart;
    }

    @Override
    public Cart getUserCart(Long userId, String promoCode) {
        return  getUserCart(userId, promoCode, securityService.getCurrentUserOrganizationId());   
    }
    
    
    private Cart getUserCart(Long userId, String promoCode, Long orgId) {
        Cart cart = getUserCart(userId);
        if (promoCode != null && !promoCode.equals("")) {
            if (!promotionRepo.existsByCodeAndOrganization_IdAndActiveNow(promoCode, orgId)) {
                cart.setPromos(promoService.calcPromoDiscountForCart(null, cart));
                cart.getPromos().setError("Failed to apply promo code ["+ promoCode+"]");
            } else {
                cart.setPromos(promoService.calcPromoDiscountForCart(promoCode, cart));
            }
        } else {
            cart.setPromos(promoService.calcPromoDiscountForCart(promoCode, cart));
        }
        cart.setDiscount(cart.getPromos().getTotalDiscount());
        cart.setTotal(cart.getSubtotal().subtract(cart.getDiscount()));
        return cart;
    }

    @Override
    public Cart addCartItem(CartItem item, String promoCode){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        Long orgId;
        if(item.getOrgId() != null && item.getOrgId() > 0){
            orgId = item.getOrgId();
        } else {
            orgId = securityService.getCurrentUserOrganizationId();
        }
        StocksEntity stock =
                ofNullable(item.getStockId())
                        .map(id -> stockRepository.findByIdAndOrganizationId(id, orgId))
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE ,P$STO$0001,item.getStockId()));
        validateCartItem(stock, item);

        CartItemEntity cartItem =
                ofNullable(cartItemRepo.findByStock_IdAndUser_Id(stock.getId(), user.getId()))
                        .orElse(new CartItemEntity());

        if (item.getQuantity().equals(0)) {
            if (cartItem.getId() != null) {
                return deleteCartItem(cartItem.getId(), promoCode);
            } else {
                return getUserCart(user.getId(), promoCode);
            }
        }
        createCartItemEntity(cartItem, (UserEntity) user, stock, item);
        cartItemRepo.save(cartItem);

        return getUserCart(user.getId(), promoCode);
    }




    @Override
    @Transactional
    public Cart addCartItems(List<CartItem> items, String promoCode){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        Long orgId = securityService.getCurrentUserOrganizationId();
        cartItemRepo.deleteByUser_Id(user.getId());

        Map<Long, StocksEntity> stocksEntityMap = getCartStocks(items, orgId);
        List<CartItemEntity> itemsToSave = new ArrayList<>();
        for (CartItem item : items) {
            StocksEntity stock = ofNullable(stocksEntityMap.get(item.getStockId()))
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$STO$0001, item.getStockId()));
            validateCartItem(stock, item);

            if (item.getQuantity().equals(0)) {
               continue;
            }
            CartItemEntity cartItem = new CartItemEntity();
            createCartItemEntity(cartItem, (UserEntity) user, stock, item);
            itemsToSave.add(cartItem);
        }
        cartItemRepo.saveAll(itemsToSave);
        return getUserCart(user.getId(), promoCode);
    }

    private void createCartItemEntity(CartItemEntity cartItem, UserEntity user, StocksEntity stock, CartItem item) {
        String additionalDataJson = cartServiceHelper.getAdditionalDataJsonString(item);
        cartItem.setUser(user);
        cartItem.setStock(stock);
        cartItem.setQuantity(item.getQuantity());
        cartItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
        cartItem.setAdditionalData(additionalDataJson);
    }

    private Map<Long, StocksEntity> getCartStocks(List<CartItem> items, Long orgId) {
        Set<Long> cartStockIds = items
                .stream()
                .map(CartItem::getStockId)
                .collect(Collectors.toSet());
        return stockRepository
                .findByIdInAndOrganizationEntity_Id(cartStockIds, orgId)
                .stream()
                .collect(toMap(StocksEntity::getId, s -> s));
    }

    private String getItemCoverImage(String coverImage, StocksEntity stock) {
        if (coverImage != null) {
            return coverImage;
        }
        Long productId = stock.getProductVariantsEntity().getProductEntity().getId();
        Long variantId = stock.getProductVariantsEntity().getId();
        return ofNullable(imgService.getProductsAndVariantsImages(asList(productId), asList(variantId))
                .stream()
                .findFirst())
                .get()
                .orElse(new ProductImageDTO())
                .getImagePath();
    }



    @Override
    public Cart deleteCartItem(Long itemId, String promoCode){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());

        return getUserCart(user.getId(), promoCode);
    }



    @Override
    public Order checkoutCart(CartCheckoutDTO dto) {
        Long userId = securityService.getCurrentUser().getId();
        TierEntity tierEntity = tierServiceImp.getTierByAmount(orderService.countOrdersByUserId(userId));
        UserEntity userEntity = userRepository.findById(userId).get();
        userEntity.setTier(tierEntity);
        userRepository.save(userEntity);
        if (userEntity.getFamily() != null) {
            Long familyId = userEntity.getFamily().getId();
            Long orgId = securityService.getCurrentUserOrganizationId();
            List<UserEntity> users = userRepository.getByFamily_IdAndOrganizationId(familyId, orgId);
            for (UserEntity user : users) {
                Long userFamilyId = user.getId();
                coinsDropService.giveUserCoinsNewFamilyPurchase(userFamilyId);
            }
        }
        //
        updateUserBoosterByPurchaseSize();
        //
        return orderService.createOrder(dto);
    }



    @Override
    public List<ShopFulfillingCart> getShopsThatCanProvideCartItems(){
        Long userId = securityService.getCurrentUser().getId();
        return cartItemRepo
                .getAllCartStocks(userId)
                .stream()
                .collect(groupingBy(CartItemStock::getShopId))
                .entrySet()
                .stream()
                .map(this::createShopFulfillingCart)
                .collect(toList());
    }





    @Override
    public List<ShopFulfillingCart> getShopsThatCanProvideWholeCart(){
        //it uses an additional query but gives more insurance than calculating variants from
        //cartItemsStocks
        Set<Long> cartItemVariants =
                getCart(null)
                        .getItems()
                        .stream()
                        .map(CartItem::getVariantId)
                        .collect(toSet());
        return getShopsThatCanProvideCartItems()
                .stream()
                .filter(shop -> hasAllCartVariants(shop, cartItemVariants))
                .collect(toList());
    }



    private boolean hasAllCartVariants(ShopFulfillingCart shop, Set<Long> cartItemVariants) {
        List<Long> shopCartVariants =
                ofNullable(shop)
                        .map(ShopFulfillingCart::getCartItems)
                        .orElse(emptyList())
                        .stream()
                        .map(CartItemStock::getVariantId)
                        .collect(toList());
        return cartItemVariants
                .stream()
                .allMatch(shopCartVariants::contains);
    }




    private ShopFulfillingCart createShopFulfillingCart(
            Map.Entry<Long, List<CartItemStock>> shopWithStocks) {
        Long shopId = shopWithStocks.getKey();
        List<CartItemStock> itemStocks = shopWithStocks.getValue();
        Long cityId =
                itemStocks
                        .stream()
                        .map(CartItemStock::getShopCityId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ADDR$ADDR$0005));
        return new ShopFulfillingCart(shopId, cityId, itemStocks);
    }



    @Override
    public BigDecimal calculateCartTotal(Cart cart) {
        return ofNullable(cart)
                .map(Cart::getItems)
                .map(this::calculateCartTotal)
                .orElse(ZERO);
    }




    public BigDecimal calculateCartTotal(List<CartItem> cartItems) {
        return  cartItems
                .stream()
                .map(item ->
                        item.getPrice()
                                .subtract(nullableBigDecimal(item.getDiscount()))
                                .multiply(new BigDecimal(item.getQuantity())))
                .reduce(ZERO, BigDecimal::add);
    }




    private void validateCartItem(StocksEntity stock, CartItem item) {
        if (item.getQuantity() == null || item.getQuantity() < 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0002);
        }

        if (item.getQuantity() > stock.getQuantity()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0003);
        }

    }


    public List<CartItem> toCartItemsDto(List<CartItemEntity> cartItems) {
        return cartItems
                .stream()
                .map(this::createCartItemDto)
                .collect(toList());
    }



    private CartItem createCartItemDto(CartItemEntity itemData) {
        CartItem itemDto = new CartItem();

        StocksEntity stock = itemData.getStock();
        ProductVariantsEntity variant = stock.getProductVariantsEntity();
        ProductEntity product = variant.getProductEntity();
        BrandsEntity brand = product.getBrand();
        UserEntity user = itemData.getUser();
        String unit = ofNullable(stock.getUnit())
                .map(StockUnitEntity::getName)
                .orElse("");
        Map<String,String> variantFeatures = ofNullable(productService.parseVariantFeatures(variant, 0))
                .orElse(new HashMap<>());
        Map<String,Object> additionalData = cartServiceHelper.getAdditionalDataAsMap(itemData.getAdditionalData());

        itemDto.setBrandId( brand.getId());
        itemDto.setBrandLogo(brand.getLogo());
        itemDto.setBrandName(brand.getName());

        itemDto.setCoverImg(itemData.getCoverImage());
        itemDto.setPrice(stock.getPrice());
        itemDto.setQuantity(itemData.getQuantity());
        itemDto.setVariantFeatures(variantFeatures);
        itemDto.setName(product.getName());
        itemDto.setWeight(variant.getWeight());
        itemDto.setUnit(unit);

        itemDto.setId(itemData.getId());
        itemDto.setProductId(product.getId());
        itemDto.setVariantId(variant.getId());
        itemDto.setVariantName(variant.getName());
        itemDto.setProductType(product.getProductType());
        itemDto.setStockId(stock.getId());
        itemDto.setDiscount(stock.getDiscount());
        itemDto.setAdditionalData(additionalData);
        itemDto.setUserId(user.getId());
        itemDto.setOrgId(product.getOrganizationId());

        return itemDto;
    }

    @Override
    public void sendAbandonedCartEmails(AbandonedCartsMail dto) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        List<UserCartInfo> carts = getUsersCarts(dto, org.getId());
        if (carts.isEmpty()) {
            return;
        }
        String orgName = org.getName();
        String email = getOrganizationEmail(org.getId());
        String sendPulseId = getOrganizationEmailData("smtp_id", org.getId());
        String sendPulseKey = getOrganizationEmailData("smtp_key", org.getId());
        SendPulseService service = new SendPulseService(sendPulseId, sendPulseKey);
        for(UserCartInfo info : carts) {
            Map<String,Object> variables = createUserCartEmailBody(info, dto);
            String body = mailService.createBodyFromThymeleafTemplate(ABANDONED_CART_TEMPLATE, variables);
            service.smtpSendMail(orgName, email, info.getName(), info.getEmail(),
                    body, "Abandoned Cart at "+orgName, null);
        }
    }

    private String getOrganizationEmail(Long orgId) {
        return settingRepo.findBySettingNameAndOrganization_Id(ORG_EMAIL.name(), orgId)
                .map(SettingEntity::getSettingValue)
                .orElse(config.mailSenderAddress);
    }

    private String getOrganizationEmailData(String setting, Long orgId) {
        return settingRepo.findBySettingNameAndOrganization_Id(setting, orgId)
                .map(SettingEntity::getSettingValue)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SETTING$0001, setting));
    }

    private Map<String,Object> createUserCartEmailBody(UserCartInfo info, AbandonedCartsMail dto) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        Long orgId = org.getId();
        Map<String, Object> params = createOrgPropertiesParams(org);
        if (dto.getPromo() != null) {
            PromotionsEntity promo = promotionRepo.findByCodeAndOrganization_IdAndActiveNow(dto.getPromo(), orgId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0008, dto.getPromo()));
            var discountData = readJsonStrAsMap(promo.getDiscountJson());
            String discount = getOptionalBigDecimal(discountData, DISCOUNT_AMOUNT)
                    .map(v -> v+" "+ org.getCountry().getCurrency())
                    .orElse(getOptionalBigDecimal(discountData, DISCOUNT_PERCENT)
                            .map(v -> v+"%")
                            .orElse("0%"));
            params.put("promo", promo.getCode());
            params.put("promoValue", discount);
        }
        info.getItems().forEach(item -> {
            BigDecimal discount = ofNullable(item.getDiscount()).orElse(ZERO);
            BigDecimal totalPrice = (item.getPrice().subtract(discount)).multiply(new BigDecimal(item.getQuantity()));
            item.setPrice(totalPrice);
        });
        params.put("items", info.getItems());
        params.put("userName", info.getName());
        params.put("hasRec", false);
        return params;
    }

    private Optional<BigDecimal> getOptionalBigDecimal(Map<String, Object> map, String key) {
        return ofNullable(map.get(key)).map(BigDecimal.class::cast);
    }

    private Map<String, Object> setNumbersAsBigDecimals(Map<String, Object> initialData) {
        return initialData
                .entrySet()
                .stream()
                .map(this::doSetNumbersAsBigDecimals)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String,Object> readJsonStrAsMap(String jsonStr){
        String rectified = ofNullable(jsonStr).orElse("{}");
        try {
            Map<String,Object> initialData = objectMapper.readValue(rectified, new TypeReference<Map<String,Object>>(){});
            if (initialData == null)
                initialData = new LinkedHashMap<>();
            return setNumbersAsBigDecimals(initialData);
        } catch (Exception e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, PROMO$JSON$0001, jsonStr);
        }
    }

    private Map<String, Object> createOrgPropertiesParams(OrganizationEntity org) {
        String domain = domainService.getBackendUrl();
        String orgDomain = domainService.getOrganizationDomainAndSubDir(org.getId());
        String orgLogo = domain + "/files/"+ orderEmailHelper.getOrganizationLogo(org);
        String cartUrl = orgDomain + "/cart";
        String orgName = org.getName();
        String year = LocalDateTime.now().getYear()+"";

        Map<String, Object> params = new HashMap<>();
        params.put("orgDomain", orgDomain);
        params.put("domain", domain);
        params.put("orgName", orgName);
        params.put("orgLogo", orgLogo);
        params.put("year", year);
        params.put("cartUrl", cartUrl);
        params.put("currency", org.getCountry().getCurrency());

        return params;
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<String,Object> doSetNumbersAsBigDecimals(Map.Entry<String, Object> entry){
        return ofNullable(entry.getValue())
                .map(Object::toString)
                .filter(NumberUtils::isParsable)
                .map(BigDecimal::new)
                .map(Object.class::cast)
                .map(val -> new AbstractMap.SimpleEntry<>(entry.getKey(), val))
                .map(Map.Entry.class::cast)
                .orElse(entry);
    }

    private List<UserCartInfo> getUsersCarts(AbandonedCartsMail dto, Long orgId) {
        List<CartItemEntity> cartEntities;
        if (!isNullOrEmpty(dto.getUserIds())) {
            cartEntities = cartItemRepo.findCartsByUsersIdAndOrg_Id(dto.getUserIds(), orgId);
        } else {
            cartEntities = cartItemRepo.findUsersCartsOrg_Id(orgId);
        }
        return cartEntities
                .stream()
                .collect(groupingBy(CartItemEntity::getUser))
                .entrySet()
                .stream()
                .map(statisticsService::toUserCartInfo)
                .collect(toList());
    }
    private void updateUserBoosterByPurchaseSize() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        UserEntity userEntity = (UserEntity) securityService.getCurrentUser();
        Integer purchaseCount = metaOrderRepository.countByUser_IdAndOrganization_IdAAndFinalizeStatus(userEntity.getId(), orgId);
        BoosterEntity boosterEntity = null;
        BoosterEntity userBoosterEntity = null;
        List<BoosterEntity> boosterList = new ArrayList<>();
        if (userEntity.getBooster() != null) {
            userBoosterEntity = userEntity.getBooster();
        }
        boosterList = boosterRepository.getAllByPurchaseSize(purchaseCount);
        int boosterSize = boosterList.size();
        if (boosterSize > 0) {
            boosterEntity = boosterList.get(boosterSize - 1);
            if (userBoosterEntity != null && userBoosterEntity != boosterEntity) {
                if (userBoosterEntity.getLevelBooster() > boosterEntity.getLevelBooster()) {
                    return;
                }
            }
            userEntity.setBooster(boosterEntity);
        }
        userRepository.save(userEntity);
    }

    @Override
    public Cart deleteYeshteryCartItem(Long itemId, String promoCode){
            BaseUserEntity user = securityService.getCurrentUser();
            if(user instanceof EmployeeUserEntity) {
                throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
            }
            cartItemRepo.deleteByIdAndUser_IdAndStock_Id(itemId, user.getId());
            return getUserCart(user.getId(), promoCode);
    }

    @Override
    public Order checkoutYeshteryCart(CartCheckoutDTO dto) {
        return orderService.createYeshteryOrder(dto);
    }

}
