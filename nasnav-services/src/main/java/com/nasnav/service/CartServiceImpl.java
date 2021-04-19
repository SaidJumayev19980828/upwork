package com.nasnav.service;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartItemData;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.helpers.CartServiceHelper;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.nasnav.commons.utils.MathUtils.nullableBigDecimal;
import static com.nasnav.exceptions.ErrorCodes.*;
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
    private CartItemRepository cartItemRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductImageService imgService;

    @Autowired
    private OrderService orderService;
    @Autowired
    private PromotionsService promotionsService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartServiceHelper cartServiceHelper;

    @Override
    public Cart getCart(String promoCode) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
        return getUserCart(user.getId(), promoCode);
    }

    @Override
    public Cart getCart() {
        return getCart(null);
    }



    @Override
    public Cart getUserCart(Long userId, String promoCode) {
        List<CartItemData> cartItemData = cartItemRepo.findCurrentCartItemsByUser_Id(userId);
        Cart cart = new Cart(toCartItemsDto(cartItemData));
        Long orgId = userRepository.findUserOrganizationId(userId);
        BigDecimal subTotal = calculateCartTotal(cart);
        Long totalCartVariantsCount = cartItemRepo.findTotalCartQuantityByUser_Id(userId);

        BigDecimal discount = promotionsService.calculateAllApplicablePromos(cartItemData, userId, orgId, subTotal, totalCartVariantsCount, promoCode);

        BigDecimal total = subTotal.subtract(discount);
        cart.setSubTotal(subTotal);
        cart.setDiscount(discount);
        cart.setTotal(total);
        cart.getItems().forEach(cartServiceHelper::replaceProductIdWithGivenProductId);
        cart.getItems().forEach(cartServiceHelper::addProductTypeFromAdditionalData);
        return cart;
    }

    @Override
    public Cart addCartItem(CartItem item) {
        return addCartItem(item, null);
    }


    @Override
    public Cart addCartItem(CartItem item, String promoCode){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        Long orgId = securityService.getCurrentUserOrganizationId();
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

        String additionalDataJson = cartServiceHelper.getAdditionalDataJsonString(item);

        cartItem.setUser((UserEntity) user);
        cartItem.setStock(stock);
        cartItem.setQuantity(item.getQuantity());
        cartItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
        cartItem.setAdditionalData(additionalDataJson);
        cartItemRepo.save(cartItem);

        return getUserCart(user.getId(), promoCode);
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
    public Order checkoutCart(CartCheckoutDTO dto){
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
                getCart()
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




    private BigDecimal calculateCartTotal(Cart cart) {
        return ofNullable(cart)
                .map(Cart::getItems)
                .map(this::calculateCartTotal)
                .orElse(ZERO);
    }




    private BigDecimal calculateCartTotal(List<CartItem> cartItems) {
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


    public List<CartItem> toCartItemsDto(List<CartItemData> cartItems) {
        return cartItems
                .stream()
                .map(this::createCartItemDto)
                .collect(toList());
    }





    private Map<String, String> parseVariantFeatures(String featureSpec, Integer returnedName) {
        return productService.parseVariantFeatures(featureSpec, returnedName);
    }



    private CartItem createCartItemDto(CartItemData itemData) {
        CartItem itemDto = new CartItem();

        Map<String,String> variantFeatures = parseVariantFeatures(itemData.getFeatureSpec(), 0);
        Map<String,Object> additionalData = cartServiceHelper.getAdditionalDataAsMap(itemData.getAdditionalData());

        itemDto.setBrandId(itemData.getBrandId());
        itemDto.setBrandLogo(itemData.getBrandLogo());
        itemDto.setBrandName(itemData.getBrandName());

        itemDto.setCoverImg(itemData.getCoverImg());
        itemDto.setPrice(itemData.getPrice());
        itemDto.setQuantity(itemData.getQuantity());
        itemDto.setVariantFeatures(variantFeatures);
        itemDto.setName(itemData.getProductName());
        itemDto.setWeight(itemData.getWeight());
        itemDto.setUnit(itemData.getUnit());

        itemDto.setId(itemData.getId());
        itemDto.setProductId(itemData.getProductId());
        itemDto.setVariantId(itemData.getVariantId());
        itemDto.setVariantName(itemData.getVariantName());
        itemDto.setProductType(itemData.getProductType());
        itemDto.setStockId(itemData.getStockId());
        itemDto.setDiscount(itemData.getDiscount());
        itemDto.setAdditionalData(additionalData);
        itemDto.setUserId(itemData.getUserId());
        return itemDto;
    }


    //TODO: this implementation should be more efficient in accessing the database and
    //addCartItem should be the one depending on it instead.
    //also it can be the foundation for POST /cart api that saves the whole cart at once.
    private Cart saveCart(Cart cart, String promoCode) {
        BaseUserEntity user = securityService.getCurrentUser();
        return cart
                .getItems()
                .stream()
                .map(this::addCartItem)
                .reduce((first, second) -> second)
                .orElseGet(() -> getUserCart(user.getId(), promoCode));
    }



    private Cart replaceCart(Cart newCart, String promoCode) {
        BaseUserEntity user = securityService.getCurrentUser();
        cartItemRepo.deleteByUser_Id(user.getId());
        return saveCart(newCart, promoCode);
    }


}
