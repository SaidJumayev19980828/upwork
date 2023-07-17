package com.nasnav.service.impl;

import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.MathUtils.nullableBigDecimal;
import static com.nasnav.constatnts.EmailConstants.ABANDONED_CART_TEMPLATE;
import static com.nasnav.enumerations.Settings.ORG_EMAIL;

import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.persistence.PromotionsEntity.DISCOUNT_AMOUNT;
import static com.nasnav.persistence.PromotionsEntity.DISCOUNT_PERCENT;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nasnav.dao.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.CartItemAddonDetailsDTO;
import com.nasnav.dto.Pair;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.UserCartInfo;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddonEntity;
import com.nasnav.persistence.AddonStocksEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.CartItemAddonDetailsEntity;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.SettingEntity;
import com.nasnav.persistence.StockUnitEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.CartService;
import com.nasnav.service.DomainService;
import com.nasnav.service.LoyaltyPointsService;
import com.nasnav.service.MailService;
import com.nasnav.service.OrderEmailServiceHelper;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.ProductService;
import com.nasnav.service.PromotionsService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.StatisticsService;
import com.nasnav.service.helpers.CartServiceHelper;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import com.nasnav.service.sendpulse.SendPulseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final Logger logger = LogManager.getLogger();

    private final SecurityService securityService;
    private final PromotionsService promoService;
    private final OrganizationRepository organizationRepo;
    private final CartItemRepository cartItemRepo;
    private final WishlistItemRepository wishlistRepo;
    private final PromotionRepository promotionRepo;
    private final ProductService productService;
    private final StockRepository stockRepository;
    private final ProductImageService imgService;
    private final DomainService domainService;
    private final StatisticsService statisticsService;
    private final MailService mailService;
    private final OrderEmailServiceHelper orderEmailHelper;
    private final CartServiceHelper cartServiceHelper;
    private final ObjectMapper objectMapper;
    private final SettingRepository settingRepo;
    private final AppConfig config;
    private final LoyaltyPointsService loyaltyPointsService;
    private final  CartItemAddonDetailsRepository cartItemAddonDetailsRepository;
    private final  AddonStockRepository addonStockRepository;
  
    private final  AddonsRepository addonsRepository;
    @Autowired
    private UserRepository userRepo;
    @Override
    public AppliedPromotionsResponse getCartPromotions(String promoCode) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
        Cart cart = getUserCart(user.getId());
        return promoService.calcPromoDiscountForCart(promoCode, cart);
    }

    @Override
    public Cart getCart(String promoCode, Set<Long> points, boolean yeshteryCart) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
        return getUserCart(user.getId(), promoCode, points, yeshteryCart);
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
    public Cart getUserCart(Long userId,Boolean isYeshtery) {
        Long authUserOrgId = securityService.getCurrentUserOrganizationId();
        Long organizationId  = userRepo.getOne(userId).getOrganizationId();
        if(!authUserOrgId.equals(organizationId)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0005, organizationId);
        }
        return getUserCart(userId, null, authUserOrgId, emptySet(), false);
    }

    @Override
    public Cart getUserCart(Long userId, String promoCode, Set<Long> points, boolean yeshteryCart) {
        return  getUserCart(userId, promoCode, securityService.getCurrentUserOrganizationId(), points, yeshteryCart);
    }
    
    
    private Cart getUserCart(Long userId, String promoCode, Long orgId, Set<Long> points, boolean yeshteryCart) {
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
        if (points != null && points.size() > 0) {
            cart.setPoints(loyaltyPointsService.calculateCartPointsDiscount(cart.getItems(), points, yeshteryCart));
        } else {
            
        }
        
        cart.setDiscount(cart.getPromos().getTotalDiscount().add(cart.getPoints().getTotalDiscount()));
        cart.setTotal(cart.getSubtotal().subtract(cart.getDiscount()));
        return cart;
    }

    @Override
    public Cart addCartItem(CartItem item, String promoCode, Set<Long> points, boolean yeshteryCart){
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
                return deleteCartItem(cartItem.getId(), promoCode, points, yeshteryCart);
            } else {
                return getUserCart(user.getId(), promoCode, points, yeshteryCart);
            }
        }
        createCartItemEntity(cartItem, (UserEntity) user, stock, item);
       
        if(item.getAddonList() !=null &&!item.getAddonList().isEmpty()) {

        	Set<CartItemAddonDetailsEntity> addonList=addCartItemAddons(cartItem, item,(UserEntity) user);
        	cartItem.setAddons(addonList);
        	
        }
        cartItemRepo.save(cartItem);
        
        return getUserCart(user.getId(), promoCode, points, yeshteryCart);
    }




    @Override
    @Transactional
    public Cart addNasnavCartItems(List<CartItem> items, String promoCode, Set<Long> points, boolean yeshteryCart) {
        BaseUserEntity user = securityService.getCurrentUser();
        if (user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        Long orgId = securityService.getCurrentUserOrganizationId();
        cartItemRepo.deleteByUser_Id(user.getId());
        cartItemAddonDetailsRepository.deleteByUserId(user.getId());

        return addCartItems(items, promoCode, Set.of(orgId), (UserEntity) user, points, yeshteryCart);
    }

    @Override
    @Transactional
    public Cart addYeshteryCartItems(List<CartItem> items, String promoCode, Set<Long> points, boolean yeshteryCart){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
        cartItemRepo.deleteByUser_Id(user.getId());
        Set<Long> yeshteryOrgsIds = organizationRepo.findIdByYeshteryState(1);
        return addCartItems(items, promoCode, yeshteryOrgsIds, (UserEntity) user, points, yeshteryCart);
    }

    private Cart addCartItems(List<CartItem> items, String promoCode, Set<Long> orgIds, UserEntity user, Set<Long> points, boolean yeshteryCart) {
        Map<Long, StocksEntity> stocksEntityMap = getCartStocks(items, orgIds);
        List<CartItemEntity> itemsToSave = new ArrayList<>();
        for (CartItem item : items) {
            StocksEntity stock = ofNullable(stocksEntityMap.get(item.getStockId()))
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$STO$0001, item.getStockId()));
            validateCartItem(stock, item);

            if (item.getQuantity().equals(0)) {
                continue;
            }
            CartItemEntity cartItem = new CartItemEntity();
            createCartItemEntity(cartItem, user, stock, item);
           
            if(item.getAddonList() !=null &&!item.getAddonList().isEmpty()) {
            	
            	Set<CartItemAddonDetailsEntity> addonList=addCartItemAddons(cartItem, item,user);
            	cartItem.setAddons(addonList);
            }
            

            itemsToSave.add(cartItem);
        }
       cartItemRepo.saveAll(itemsToSave);
        
        return getUserCart(user.getId(), promoCode, points, yeshteryCart);
    }

    private void createCartItemEntity(CartItemEntity cartItem, UserEntity user, StocksEntity stock, CartItem item) {
        String additionalDataJson = cartServiceHelper.getAdditionalDataJsonString(item, stock.getQuantity());
        cartItem.setUser(user);
        cartItem.setStock(stock);
        cartItem.setQuantity(item.getQuantity());
        cartItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
        cartItem.setAdditionalData(additionalDataJson);
        cartItem.setSpecialOrder(item.getSpecialOrder());
        
    }

	
	

    private Map<Long, StocksEntity> getCartStocks(List<CartItem> items, Set<Long> orgIds) {
        Set<Long> cartStockIds = items
                .stream()
                .map(CartItem::getStockId)
                .collect(Collectors.toSet());
        return stockRepository
                .findByIdInAndOrganizationEntity_IdIn(cartStockIds, orgIds)
                .stream()
                .collect(toMap(StocksEntity::getId, s -> s));
    }

    private String getItemCoverImage(String coverImage, StocksEntity stock) {
        if (coverImage != null) {
            return coverImage;
        }
        Long productId = stock.getProductVariantsEntity().getProductEntity().getId();
        Long variantId = stock.getProductVariantsEntity().getId();
        return Optional.of(imgService.getProductsAndVariantsImages(List.of(productId), List.of(variantId))
                .stream()
                .findFirst())
                .get()
                .orElse(new ProductImageDTO())
                .getImagePath();
    }



    @Override
    public Cart deleteCartItem(Long itemId, String promoCode, Set<Long> points, boolean yeshteryCart){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());
        cartItemAddonDetailsRepository.deleteByCartItemEntity_Id(itemId);

        return getUserCart(user.getId(), promoCode, points, yeshteryCart);
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
    public List<ShopFulfillingCart> getSelectedShopsThatCanProvideCartItems(List<Long> shops){
        Long userId = securityService.getCurrentUser().getId();
        return cartItemRepo
                .getAllCartStocks(userId, shops)
                .stream()
                .distinct()
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
                getCart(null, emptySet(), false)
                        .getItems()
                        .stream()
                        .map(CartItem::getVariantId)
                        .collect(toSet());
        return getShopsThatCanProvideCartItems()
                .stream()
                .filter(shop -> hasAllCartVariants(shop, cartItemVariants))
                .collect(toList());
    }

    @Override
    public List<ShopRepresentationObject> getShopsThatCanProvideEachItem() {
        Long userId = securityService.getCurrentUser().getId();
        List<ProductVariantsEntity> variants = cartItemRepo.findCurrentCartVariantsByUser_Id(userId);
        Map<Long, Set<Long>> variantsIdsPerShop = cartItemRepo.findCartVariantAndShopPairByUser_Id(userId)
                .stream()
                .collect(groupingBy(Pair::getFirst, mapping(Pair::getSecond, toSet())));
        return variants
                .stream()
                .map(ProductVariantsEntity::getStocks)
                .flatMap(Set::stream)
                .map(StocksEntity::getShopsEntity)
                .distinct()
                .map(shop -> (ShopRepresentationObject) shop.getRepresentation())
                .map(shop -> setShopVariants(shop, variantsIdsPerShop))
                .filter(shop -> shop.getVariantIds() != null)
                .collect(toList());
    }

    private ShopRepresentationObject setShopVariants(ShopRepresentationObject shop, Map<Long, Set<Long>> variantsIdsPerShop) {
        if (variantsIdsPerShop.containsKey(shop.getId()))
            shop.setVariantIds(variantsIdsPerShop.get(shop.getId()));
        return shop;
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
        Long orgId = itemStocks
                .stream()
                .map(CartItemStock::getOrgId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ORG$SHIP$0002));
        return new ShopFulfillingCart(shopId, cityId, orgId, itemStocks);
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
                                .multiply(new BigDecimal(item.getQuantity())).add(calculateAddonsTotal(item)))
                .reduce(ZERO, BigDecimal::add);
    }
  public BigDecimal calculateAddonsTotal(CartItem cartItem) {
    	
    	
         if (cartItem.getAddonList()!=null &&!cartItem.getAddonList().isEmpty()) {
        return	 cartItem.getAddonList()
                .stream()
                .map(item ->
                        item.getPrice()
                                
                                .multiply(new BigDecimal(cartItem.getQuantity())))
                .reduce(ZERO, BigDecimal::add);
         }
        	 return new BigDecimal(0);
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
        
        if(itemData.getAddons()!=null && itemData.getAddons().size()>0) {
        List<CartItemAddonDetailsDTO> addonList=new ArrayList<>();
        for(CartItemAddonDetailsEntity addon:itemData.getAddons()) {
        	AddonStocksEntity addonStock=addonStockRepository.getOne(addon.getAddonStockEntity().getId());
        	CartItemAddonDetailsDTO dto=new CartItemAddonDetailsDTO();
        	dto.setAddonStockId(addonStock.getId());
        	
        	Optional<AddonEntity> addonEntity=addonsRepository.findById(addonStock.getAddonEntity().getId());
        	addonEntity.ifPresent(addonItem -> dto.setAddoneName(addonItem.getName()));
        	addonEntity.ifPresent(addonItem -> dto.setType(addonItem.getType()));
        	dto.setPrice(addonStock.getPrice());
        	dto.setAddonItemId(addon.getId());
        	
        	addonList.add(dto);
        	
        }
        
        itemDto.setAddonList(addonList);
        }
        itemDto.setSpecialOrder(itemData.getSpecialOrder());
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

    @Override
    public Cart deleteYeshteryCartItem(Long itemId, String promoCode, Set<Long> points, boolean yeshteryCart){
            BaseUserEntity user = securityService.getCurrentUser();
            if(user instanceof EmployeeUserEntity) {
                throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
            }
            cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());
            return getUserCart(user.getId(), promoCode, points, yeshteryCart);
    }

    @Scheduled(fixedRate = 864000000)
    @Transactional
    public void moveOutOfStockCartItemsToWishlist() {
        List<CartItemEntity> cartItems = cartItemRepo.findOutOfStockCartItems();
        List<CartItemEntity> movedItems = new ArrayList<>();
        Set<Long> cartItemsIds = new HashSet<>();

        for (CartItemEntity item : cartItems) {
            var stock = item
                .getStock()
                .getProductVariantsEntity()
                .getStocks()
                .stream()
                .filter(s -> s.getQuantity() != null && s.getQuantity() > 0)
                .findFirst();
            if (stock.isPresent()) {
                item.setStock(stock.get());
            } else {
                movedItems.add(item);
                cartItemsIds.add(item.getId());
            }
        }
        if (!cartItems.isEmpty()) {
            cartItemRepo.saveAll(cartItems);
            moveCartItemsToWishlist(movedItems);
            logger.info(format("moved %d items to wishlist", cartItemsIds.size()));
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void moveCartItemsToWishlist(List<CartItemEntity> allMovedItems) {
        Set<Long> itemsToWishlist = getItemsToWishlist(allMovedItems);
        Set<Long> itemsToRemove = getItemsToRemoveFromCart(allMovedItems, itemsToWishlist);

        allMovedItems.forEach(cartServiceHelper::addOutOfStockFlag);
        cartItemRepo.saveAll(allMovedItems);
        cartItemRepo.moveCartItemsToWishlistItems(itemsToWishlist);
        cartItemRepo.deleteByCartItemId(itemsToRemove);
    }

    private Set<Long> getItemsToWishlist(List<CartItemEntity> movedItems) {
        Long currentUserId = securityService.getCurrentUser().getId();
        List<Long> wishlistStocks = wishlistRepo.getAllWishlistStocks(currentUserId);

        return movedItems
                    .stream()
                    .filter(cartItem -> ! wishlistStocks.contains(cartItem.getStock().getId()))
                    .map(CartItemEntity::getId)
                    .collect(toSet());
    }

    private Set<Long> getItemsToRemoveFromCart(List<CartItemEntity> allMovedItems, Set<Long> itemsToWishlist) {
        return allMovedItems
                    .stream()
                    .filter(cartItem -> ! itemsToWishlist.contains(cartItem))
                    .map(CartItemEntity::getId)
                    .collect(toSet());
    }
    
    private void validateAddonItem(AddonStocksEntity stock, Integer itemQuantity,Long shopId) {
    	 if (stock.getShopsEntity().getId()!=shopId) {
             throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$ADDON$0003);
         }
        if (itemQuantity == null || itemQuantity < 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0002);
        }

        if (itemQuantity > stock.getQuantity()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0003);
        }

    }
    
    private Set<CartItemAddonDetailsEntity> addCartItemAddons(CartItemEntity cartItem, CartItem item,UserEntity user) {
		Set<CartItemAddonDetailsEntity> list=new HashSet<>();
		if (!item.getAddonList().isEmpty()) {

			for (CartItemAddonDetailsDTO dto : item.getAddonList()) {
				CartItemAddonDetailsEntity addon = cartItemAddonDetailsRepository.findByCartItemEntity_IdAndAddonStockEntity_Id(cartItem.getId(), dto.getAddonStockId());
				if(addon==null) {
					addon=new CartItemAddonDetailsEntity();
				}
				AddonStocksEntity addonstock = ofNullable(dto.getAddonStockId())
						.map(id -> addonStockRepository.getOne(dto.getAddonStockId()))
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$ADDON$0002,
								dto.getAddonStockId()));
				validateAddonItem(addonstock, cartItem.getQuantity(),cartItem.getStock().getShopsEntity().getId());
                 addon.setCartItemEntity(cartItem);
				addon.setAddonStockEntity(addonstock);
				addon.setUser(user);
				list.add(addon);

			}

		}
		return list;
	}
}
