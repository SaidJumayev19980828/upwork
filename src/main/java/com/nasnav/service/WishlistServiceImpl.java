package com.nasnav.service;

import com.nasnav.dao.StockRepository;
import com.nasnav.dao.WishlistItemRepository;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartItemData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        wishlistItem.setUser((UserEntity) user);
        wishlistItem.setStock(stock);
        wishlistItem.setQuantity(item.getQuantity());
        wishlistItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
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
        return new Wishlist(toCartItemsDto(wishlistRepo.findCurrentCartItemsByUser_Id(userId)));
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

        Integer qty = ofNullable(item.getQuantity()).orElse(1);
        wishlistRepo.moveToCart(itemId, qty, user.getId());
        return cartService.getCart();
    }



    private List<WishlistItem> toCartItemsDto(List<CartItemData> cartItems) {
        return cartItems
                .stream()
                .map(this::createWishlistItemDto)
                .collect(toList());
    }





    private WishlistItem createWishlistItemDto(CartItemData itemData) {
        WishlistItem itemDto = new WishlistItem();

        Map<String,String> variantFeatures = parseVariantFeatures(itemData.getFeatureSpec(), 0);

        itemDto.setBrandId(itemData.getBrandId());
        itemDto.setBrandLogo(itemData.getBrandLogo());
        itemDto.setBrandName(itemData.getBrandName());

        itemDto.setCoverImg(itemData.getCoverImg());
        itemDto.setPrice(itemData.getPrice());
        itemDto.setQuantity(itemData.getQuantity());
        itemDto.setVariantFeatures(variantFeatures);
        itemDto.setName(itemData.getProductName());

        itemDto.setId(itemData.getId());
        itemDto.setProductId(itemData.getProductId());
        itemDto.setVariantId(itemData.getVariantId());
        itemDto.setVariantName(itemData.getVariantName());
        itemDto.setProductType(itemData.getProductType());
        itemDto.setStockId(itemData.getStockId());
        itemDto.setDiscount(itemData.getDiscount());

        return itemDto;
    }



    private Map<String, String> parseVariantFeatures(String featureSpec, Integer returnedName) {
        return productService.parseVariantFeatures(featureSpec, returnedName);
    }
}
