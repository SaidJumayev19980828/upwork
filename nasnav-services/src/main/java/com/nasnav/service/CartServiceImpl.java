package com.nasnav.service;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.StockRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    private CartServiceHelper cartServiceHelper;

    @Override
    public Cart getCart() {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }
        return getUserCart(user.getId());
    }




    @Override
    public Cart getUserCart(Long userId) {
        Cart cart = new Cart(toCartItemsDto(cartItemRepo.findCurrentCartItemsByUser_Id(userId)));
        cart.getItems().forEach(cartServiceHelper::replaceProductIdWithGivenProductId);
        cart.getItems().forEach(cartServiceHelper::addProductTypeFromAdditionalData);
        return cart;
    }





    @Override
    public Cart addCartItem(CartItem item){
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
                return deleteCartItem(cartItem.getId());
            } else {
                return getUserCart(user.getId());
            }
        }
        cartItem = createCartItemEntity(cartItem, (UserEntity) user, stock, item);
        cartItemRepo.save(cartItem);

        return getUserCart(user.getId());
    }




    @Override
    @Transactional
    public Cart addCartItems(List<CartItem> items){
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
            cartItem = createCartItemEntity(cartItem, (UserEntity) user, stock, item);
            itemsToSave.add(cartItem);
        }
        cartItemRepo.saveAll(itemsToSave);
        return getUserCart(user.getId());
    }

    private CartItemEntity createCartItemEntity(CartItemEntity cartItem, UserEntity user, StocksEntity stock, CartItem item) {
        String additionalDataJson = cartServiceHelper.getAdditionalDataJsonString(item);
        cartItem.setUser(user);
        cartItem.setStock(stock);
        cartItem.setQuantity(item.getQuantity());
        cartItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
        cartItem.setAdditionalData(additionalDataJson);
        return cartItem;
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
    public Cart deleteCartItem(Long itemId){
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
        }

        cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());

        return getUserCart(user.getId());
    }



    @Override
    public Order checkoutCart(CartCheckoutDTO dto){
        return orderService.createOrder(dto);
    }



    @Override
    public BigDecimal calculateCartTotal() {
        return  calculateCartTotal(getCart());
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

        return itemDto;
    }


    //TODO: this implementation should be more efficient in accessing the database and
    //addCartItem should be the one depending on it instead.
    //also it can be the foundation for POST /cart api that saves the whole cart at once.
    private Cart saveCart(Cart cart) {
        BaseUserEntity user = securityService.getCurrentUser();
        return cart
                .getItems()
                .stream()
                .map(this::addCartItem)
                .reduce((first, second) -> second)
                .orElseGet(() -> getUserCart(user.getId()));
    }



    private Cart replaceCart(Cart newCart) {
        BaseUserEntity user = securityService.getCurrentUser();
        cartItemRepo.deleteByUser_Id(user.getId());
        return saveCart(newCart);
    }


}
