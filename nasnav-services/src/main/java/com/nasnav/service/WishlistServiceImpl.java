package com.nasnav.service;

import com.nasnav.dao.StockRepository;
import com.nasnav.dao.WishlistItemRepository;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartItemData;
import com.nasnav.service.helpers.CartServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class WishlistServiceImpl implements WishlistService{

    @Autowired
    private ProductService productService;

    @Autowired
    private WishlistItemRepository wishlistRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private StockRepository stockRepo;


    @Autowired
    private ProductImageService imgService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartServiceHelper cartServiceHelper;

    @Override
    @Transactional
    public Wishlist addWishlistItem(WishlistItem item) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$WISH$0001);
        }

        Long orgId = securityService.getCurrentUserOrganizationId();
        StocksEntity stock =
                ofNullable(item.getStockId())
                        .map(id -> stockRepo.findByIdAndOrganizationId(id, orgId))
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE ,P$STO$0001,item.getStockId()));

        WishlistItemEntity wishlistItem =
                ofNullable(wishlistRepo.findByStock_IdAndUser_Id(stock.getId(), user.getId()))
                        .orElse(new WishlistItemEntity());

        String additionalDataJson = cartServiceHelper.getAdditionalDataJsonString(item);

        wishlistItem.setUser((UserEntity) user);
        wishlistItem.setStock(stock);
        wishlistItem.setQuantity(item.getQuantity());
        wishlistItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
        wishlistItem.setAdditionalData(additionalDataJson);
        wishlistRepo.save(wishlistItem);

        return getWishlist();
    }




    private String getItemCoverImage(String coverImage, StocksEntity stock) {
        if (coverImage != null) {
            return coverImage;
        }
        Long productId = stock.getProductVariantsEntity().getProductEntity().getId();
        Long variantId = stock.getProductVariantsEntity().getId();
        return imgService
                .getProductsAndVariantsImages(asList(productId), asList(variantId))
                .stream()
                .map(ProductImageDTO::getImagePath)
                .findFirst()
                .orElse("");
    }



    @Override
    @Transactional
    public Wishlist deleteWishlistItem(Long itemId) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$WISH$0001);
        }

        wishlistRepo.deleteByIdAndUser_Id(itemId, user.getId());

        return getWishlist();
    }




    @Override
    public Wishlist getWishlist() {
        Long userId = securityService.getCurrentUser().getId();
        Wishlist wishlist = new Wishlist(toCartItemsDto(wishlistRepo.findCurrentCartItemsByUser_Id(userId)));
        wishlist.getItems().forEach(cartServiceHelper::replaceProductIdWithGivenProductId);
        wishlist.getItems().forEach(cartServiceHelper::addProductTypeFromAdditionalData);
        return wishlist;
    }




    @Override
    @Transactional
    public Cart moveWishlistItemsToCart(WishlistItemQuantity item) {
        BaseUserEntity user = securityService.getCurrentUser();
        if(user instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, O$WISH$0001);
        }

        Long itemId = item.getItemId();
        if(!wishlistRepo.existsByIdAndUser_Id(itemId, user.getId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE ,O$WISH$0002 ,itemId);
        }
        Long stockId = wishlistRepo.findWishlistItemStockId(itemId, user.getId());
        Integer qty = ofNullable(item.getQuantity()).orElse(1);
        CartItem cartItem = new CartItem(stockId, qty, item.getAdditionalData());
        return cartService.addCartItem(cartItem, null);
    }

    @Override
    @Transactional
    public Wishlist addYeshteryWishlistItem(WishlistItem item) {
        if (securityService.getYeshteryState() == 1){
            return addWishlistItem(item);
        }
        return null;
    }

    @Override
    @Transactional
    public Wishlist deleteYeshteryWishlistItem(Long itemId) {
        if (securityService.getYeshteryState() == 1){
            deleteWishlistItem(itemId);
        }
        return null;
    }

    @Override
    public Wishlist getYeshteryWishlist() {
        if (securityService.getYeshteryState() == 1){
            getWishlist();
        }
        return null;
    }

    @Override
    @Transactional
    public Cart moveYeshteryWishlistItemsToCart(WishlistItemQuantity item) {
        if (securityService.getYeshteryState() == 1){
            return moveWishlistItemsToCart(item);
        }
        return null;
    }


    private List<WishlistItem> toCartItemsDto(List<WishlistItemEntity> cartItems) {
        return cartItems
                .stream()
                .map(this::createWishlistItemDto)
                .collect(toList());
    }





    private WishlistItem createWishlistItemDto(WishlistItemEntity itemData) {
        WishlistItem itemDto = new WishlistItem();

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
}
