package com.nasnav.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.TokenPayment;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.dto.response.TokenPaymentResponse;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.*;
import com.nasnav.service.helpers.CartServiceHelper;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import com.nasnav.service.sendpulse.SendPulseService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.*;
import java.net.URI;
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
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final Logger logger = LogManager.getLogger();
    private final UserRepository userRepository;
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
    private final BrandsRepository brandsRepository;
    private final  AddonsRepository addonsRepository;
    private final BankAccountActivityService  bankAccountActivityService;

    private final InfluencerReferralService influencerReferralService;

    private final StoreCheckoutsRepository storeCheckoutsRepository;
    @Autowired
    private UserRepository userRepo;

    @Value("${currency-rate-endpoint}")
    private String currenyRate ;
    @Value("${bc-endpoint}")
    private String bCEndpoint;
    @Autowired
    private ReferralCodeRepo referralCodeRepo;

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
    public Cart getCart(String promoCode, BigDecimal points, boolean yeshteryCart) {
        BaseUserEntity user = securityService.getCurrentUser();
        return getUserCart(user.getId(), promoCode, points, yeshteryCart);
    }


    @Deprecated
    @Override
    public Cart getCart(CartCheckoutDTO dto, String promoCode, Set<Long> points, boolean yeshteryCart) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
             user = userRepository.findById(dto.getCustomerId()).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,dto.getCustomerId()));
        }
        return getUserCart(user.getId(),user.getOrganizationId(), promoCode, points, yeshteryCart);
    }

    @Override
    public Cart getCart(CartCheckoutDTO dto, String promoCode, BigDecimal points, boolean yeshteryCart) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            user = userRepository.findById(dto.getCustomerId()).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,dto.getCustomerId()));
        }
        return yeshteryCart ?
                getYeshteryUserCart(dto, user.getId(), user.getOrganizationId(), promoCode, points) :
                getUserCart(user.getId(), user.getOrganizationId(), promoCode, points, false);
    }


    @Override
    public Cart getUserCart(Long userId) {
        return setupCart(new Cart(toCartItemsDto(cartItemRepo.findCurrentCartItemsByUser_Id(userId))));
    }

    public Cart getYeshteryUserCart(Long userId, Set<Long> stockIds) {
        return setupCart(new Cart(toCartItemsDto(cartItemRepo.findCurrentCartSelectedItemsByUserId(userId, stockIds))));
    }

    private Cart setupCart(Cart cart) {
        cart.getItems().forEach(cartServiceHelper::replaceProductIdWithGivenProductId);
        cart.getItems().forEach(cartServiceHelper::addProductTypeFromAdditionalData);
        cart.setSubtotal(calculateCartTotal(cart));
        return cart;
    }

    @Override
    public Cart getUserCart(Long userId, Boolean isYeshtery) {
        Long authUserOrgId = securityService.getCurrentUserOrganizationId();
        boolean userExists = userRepo.existsByIdAndOrganizationId(userId, authUserOrgId);
        if(!userExists){
            throw new RuntimeBusinessException(NOT_FOUND, E$USR$0002);
        }
        return getUserCart(userId, null, authUserOrgId, ZERO, false);
    }

    @Override
    public Cart getUserCart(Long userId, String promoCode, Set<Long> points, boolean yeshteryCart) {
        return  getUserCart(userId, promoCode, securityService.getCurrentUserOrganizationId(), points, yeshteryCart);
    }
    @Override
    public Cart getUserCart(Long userId, String promoCode, BigDecimal points, boolean yeshteryCart) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        Long organizationId = securityService.getCurrentUserOrganizationId();
        if(loggedInUser instanceof  EmployeeUserEntity) {
            StoreCheckoutsEntity storeCheckoutsEntity = storeCheckoutsRepository.findByEmployeeId(loggedInUser.getId())
                    .orElseThrow(() ->  new RuntimeBusinessException(FORBIDDEN, O$CRT$0001));
            userId = storeCheckoutsEntity.getUserId();
        }
        return getUserCart(userId, promoCode,organizationId , points, yeshteryCart);
    }

    public Cart getUserCart(Long userId,Long orgId ,String promoCode, Set<Long> points, boolean yeshteryCart) {
        return  getUserCart(userId, promoCode, orgId, points, yeshteryCart);
    }

    public Cart getUserCart(Long userId,Long orgId ,String promoCode, BigDecimal points, boolean yeshteryCart) {
        return  getUserCart(userId, promoCode, orgId, points, yeshteryCart);
    }


    private Cart getUserCart(Long userId, String promoCode, Long orgId, BigDecimal points, boolean yeshteryCart) {
        Cart cart = getUserCart(userId);
        setupCartPromotionsAndDiscounts(promoCode, orgId, points, yeshteryCart, cart);
        return cart;
    }

    private void setupCartPromotionsAndDiscounts(String promoCode, Long orgId, BigDecimal points, boolean yeshteryCart, Cart cart) {
        BigDecimal influencerReferralDiscount = ZERO;
        if(referralCodeRepo.existsByReferralCodeAndReferralType(promoCode, ReferralType.INFLUENCER)) {
            influencerReferralDiscount = influencerReferralService.calculateDiscountForCart(promoCode, cart);
        } else {
            if (promoCode != null && !promoCode.isEmpty()) {
                if (!promotionRepo.existsByCodeAndOrganization_IdAndActiveNow(promoCode, orgId)) {
                    cart.setPromos(promoService.calcPromoDiscountForCart(null, cart));
                    cart.getPromos().setError("Failed to apply promo code [" + promoCode + "]");
                } else {
                    applyPromoCodeToCart(promoCode, cart);
                }
            } else {
                cart.setPromos(promoService.calcPromoDiscountForCart(promoCode, cart));
            }
            if (points != null && points.compareTo(ZERO) > 0) {
                cart.setPoints(loyaltyPointsService.calculateCartPointsDiscount(cart.getItems(), points, yeshteryCart));
            }
        }
        decidePromotionApplied(cart,promoCode);
        cart.setDiscount(cart.getPromos().getTotalDiscount().add(cart.getPoints().getTotalDiscount()));
        cart.setDiscount(cart.getDiscount().add(influencerReferralDiscount));
        cart.setTotal(cart.getSubtotal().subtract(cart.getDiscount()));
    }

    public Cart getYeshteryUserCart(CartCheckoutDTO dto, Long userId, Long orgId, String promoCode, BigDecimal points) {
        Cart cart = getYeshteryUserCart(userId, dto.getSelectedStockIds());
        setupCartPromotionsAndDiscounts(promoCode, orgId, points, true, cart);
        return cart;
    }

    private Cart getUserCart(Long userId, String promoCode, Long orgId, Set<Long> points, boolean yeshteryCart) {
        Cart cart = getUserCart(userId);
        if (promoCode != null && !promoCode.equals("")) {
            if (!promotionRepo.existsByCodeAndOrganization_IdAndActiveNow(promoCode, orgId)) {
                cart.setPromos(promoService.calcPromoDiscountForCart(null, cart));
                cart.getPromos().setError("Failed to apply promo code ["+ promoCode+"]");
            } else {
                applyPromoCodeToCart(promoCode, cart);
            }
        } else {
            cart.setPromos(promoService.calcPromoDiscountForCart(promoCode, cart));
        }
        if (points != null && points.size() > 0) {
            cart.setPoints(loyaltyPointsService.calculateCartPointsDiscount(cart.getItems(), points, yeshteryCart));
        } else {

        }
        decidePromotionApplied(cart,promoCode);
        cart.setDiscount(cart.getPromos().getTotalDiscount().add(cart.getPoints().getTotalDiscount()));
        cart.setTotal(cart.getSubtotal().subtract(cart.getDiscount()));
        return cart;
    }
    private void decidePromotionApplied(Cart cart , String promoCode) {
        if (cart.getPromos().getTotalDiscount().compareTo(ZERO) <= 0 && promoCode != null && !promoCode.isEmpty()){
            cart.getPromos().setError("Failed to apply promo code ["+promoCode+"]");
        }
    }
    private void applyPromoCodeToCart(String promoCode, Cart cart) {
        // Attempt to find a promotion entity by name
        Optional<PromotionsEntity> getPromoCode = promotionRepo.findByCode(promoCode);

        // Check if the promotion entity is present
        if (getPromoCode.isEmpty()) {
            // Promotion entity not found, set an error message in the cart promos
            cart.getPromos().setError("Not found promo code [" + promoCode + "]");
            return;
        }

        // Promotion entity found, proceed with applying the promo code to the cart
        PromotionsEntity promoEntity = getPromoCode.get();
        cart.setPromos(promoService.calcPromoDiscountForCart(
                promoEntity.getUsageLimiterCount() > 0 ? promoCode : null, cart));
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
                return deleteCartItem(cartItem.getId(), promoCode, points, yeshteryCart,null);
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
    public Cart deleteCartItem(Long itemId, String promoCode, Set<Long> points, boolean yeshteryCart,Long userId){
        BaseUserEntity user = getUser(userId);
        cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());
        cartItemAddonDetailsRepository.deleteByCartItemEntity_Id(itemId);

        return getUserCart(user.getId(), promoCode, points, yeshteryCart);
    }

    @Override
    public List<ShopFulfillingCart> getShopsThatCanProvideCartItems(){
        Long userId = fetchUserId();
        return cartItemRepo
                .getAllCartStocks(userId)
                .stream()
                .collect(groupingBy(CartItemStock::getShopId))
                .entrySet()
                .stream()
                .map(this::createShopFulfillingCart)
                .collect(toList());
    }

    private List<ShopFulfillingCart> getShopsThatCanProvideSelectedCartItems(List<Long> stockIds){
        Long userId = fetchUserId();
        return cartItemRepo.getAllCartSelectedStocks(userId, new HashSet<>(stockIds)).stream()
                .collect(groupingBy(CartItemStock::getShopId))
                .entrySet()
                .stream()
                .map(this::createShopFulfillingCart)
                .collect(toList());
    }

    private Long fetchUserId() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (loggedInUser instanceof EmployeeUserEntity)
            return storeCheckoutsRepository.findByEmployeeId(loggedInUser.getId())
                    .orElseThrow(() -> new RuntimeBusinessException(FORBIDDEN, O$CRT$0001)).getUserId();
        return loggedInUser.getId();
    }

    @Deprecated
    @Override
    public List<ShopFulfillingCart> getSelectedShopsThatCanProvideCartItems(List<Long> shops){
        Long userId = securityService.getCurrentUser().getId();
        return getSelectedShopsThatCanProvideCartItems(userId, shops);
    }

    @Override
    public List<ShopFulfillingCart> getSelectedShopsThatCanProvideCartItems(Long userId, List<Long> shops){
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
    public List<ShopFulfillingCart> getShopsThatCanProvideWholeCart(List<Long> stockIds) {
        //it uses an additional query but gives more insurance than calculating variants from
        //cartItemsStocks
        Set<Long> cartItemVariants =
                getCart(null, ZERO, false)
                        .getItems().stream().filter(l -> stockIds.contains(l.getStockId()))
                        .map(CartItem::getVariantId)
                        .collect(toSet());
        return getShopsThatCanProvideSelectedCartItems(stockIds)
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
        itemDto.setProductDescription(product.getDescription());
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
    public Cart deleteYeshteryCartItem(Long itemId, String promoCode, Set<Long> points, boolean yeshteryCart,Long userId){
            BaseUserEntity user = getUser(userId);
            cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());
            return getUserCart(user.getId(),user.getOrganizationId() ,promoCode, points, yeshteryCart);
    }

    @Scheduled(fixedRate = 864_000_000)
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

    @Override
    public TokenPaymentResponse tokenPayment(Long brandId ,TokenValueRequest request){
        String url = bCEndpoint + "send-to-brand";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<TokenPayment> entity = new HttpEntity<>(prepareRequest(brandId,request), headers);
        return callBC(url,entity);
    }
    private TokenPaymentResponse callBC(String url , HttpEntity<TokenPayment> entity){
        try{
            ResponseEntity<TokenPaymentResponse> responseEntity = new RestTemplate().exchange(URI.create(url), HttpMethod.POST,entity, TokenPaymentResponse.class);
            return responseEntity.getBody();
        }catch (Exception e){
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR,BC$001,e.getMessage());
        }
    }

    private TokenPayment prepareRequest(Long brandId, TokenValueRequest request){
        String usdAmount = fromLocalCurrencyToDollar(request).toString();
       return new TokenPayment(getUserTokenBalance().toString(), organizationBankAccount(brandId), usdAmount);
    }

    private BigDecimal getUserTokenBalance(){
        BaseUserEntity baseUser = securityService.getCurrentUser();
        if(baseUser instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
       return  userTotalBalance((UserEntity) baseUser);
    }

    private BigDecimal userTotalBalance( UserEntity user  ){
        if(user.getBankAccount() == null){
            throw new RuntimeBusinessException(NOT_FOUND, BANK$ACC$0003);
        }
        return BigDecimal.valueOf(bankAccountActivityService.getTotalBalance(user.getBankAccount().getId()));
    }
    private String organizationBankAccount(Long brandId){
        BrandsEntity brand = brandsRepository.findById(brandId).orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,GEN$0001,"brand", brandId));
      return orgWalletAddress(brand);
    }

    private String orgWalletAddress(BrandsEntity brand) {
        BankAccountEntity orgBankAccount = brand.getOrganizationEntity().getBankAccount();
        if (orgBankAccount == null) {
            throw new RuntimeBusinessException(NOT_FOUND, BANK$ACC$0009);
        }
        return orgBankAccount.getWalletAddress();
    }


    @Override
    public EstimateTokensUsdResponse estimateTokensToUsd(TokenValueRequest request) {
        return bCInquiringForEstimation(request);    /// use this one to converting usd to MVR
    }

    private EstimateTokensUsdResponse bCInquiringForEstimation(TokenValueRequest request){
        BigDecimal usdAmount = fromLocalCurrencyToDollar(request);

        String estimateEndPoint = bCEndpoint + "estimate-tokens-to-usdc";
        String url = UriComponentsBuilder.fromHttpUrl(estimateEndPoint)
                .queryParam("usdcAmount", usdAmount)
                .build()
                .encode()
                .toUriString();

        ResponseEntity<EstimateTokensUsdResponse> responseEntity = new RestTemplate().exchange(URI.create(url), HttpMethod.GET,null, EstimateTokensUsdResponse.class);
       return responseEntity.getBody();
    }

    private BigDecimal fromLocalCurrencyToDollar(TokenValueRequest request){
        return calculateCurrencyBasedOnDollar(request.getAmount(), getToDayExchangeRate(request.getCurrency()));
    }

    private BigDecimal getToDayExchangeRate(String currency){
        ResponseEntity<ExchangeRateResponse> responseEntity = new RestTemplate().exchange(URI.create(currenyRate), HttpMethod.GET,null, ExchangeRateResponse.class);
        ExchangeRateResponse response = responseEntity.getBody();
        BigDecimal usdRate = Objects.requireNonNull(response).getRates().getOrDefault(currency, BigDecimal.valueOf(48.10));
        return usdRate.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrencyBasedOnDollar(BigDecimal amount, BigDecimal usdRate){
        return amount.divide(usdRate, 10, HALF_EVEN).setScale(2,HALF_EVEN);
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
						.map(id -> addonStockRepository.getReferenceById(dto.getAddonStockId()))
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


    private BaseUserEntity getUser(Long userId) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            if (userId==null) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, NOTIUSERPARAM$0006);
            }
            return userRepository.findById(userId).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,userId));
        }
        return user;
    }
}
