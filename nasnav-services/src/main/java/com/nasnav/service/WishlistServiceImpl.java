package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.SettingRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.WishlistItemRepository;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.helpers.CartServiceHelper;
import com.nasnav.service.sendpulse.SendPulseService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.constatnts.EmailConstants.ABANDONED_CART_TEMPLATE;
import static com.nasnav.constatnts.EmailConstants.RESTOCKED_WISHLIST_TEMPLATE;
import static com.nasnav.enumerations.Settings.ORG_EMAIL;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class WishlistServiceImpl implements WishlistService{

    @Autowired
    private AppConfig config;

    @Autowired
    private ProductService productService;

    @Autowired
    private WishlistItemRepository wishlistRepo;
    @Autowired
    private DomainService domainService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    private StockRepository stockRepo;
    @Autowired
    private OrganizationRepository orgRepo;
    @Autowired
    private ProductImageService imgService;
    @Autowired
    private MailService mailService;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderEmailServiceHelper orderEmailHelper;
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

        String additionalDataJson = cartServiceHelper.getAdditionalDataJsonString(item, stock.getQuantity());

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

    // run this method every day 1728000000 in milliseconds means 2 days
    @Scheduled(fixedRate = 1728000000)
    @Override
    public void sendRestockedWishlistEmails() {
        Map<UserEntity, List<WishlistItemEntity>> usersWishlists = wishlistRepo.findUsersWishListsWithZeroStockQuantity()
                .stream()
                .collect(groupingBy(WishlistItemEntity::getUser));

        for(Map.Entry info : usersWishlists.entrySet()) {
            UserEntity user = (UserEntity) info.getKey();
            List<WishlistMailItem> items = ((List<WishlistItemEntity>) info.getValue())
                    .stream()
                    .map(this::toWishlistMailItem)
                    .collect(toList());
            OrganizationEntity org = orgRepo.findOneById(user.getOrganizationId());
            String orgName = org.getName();
            String email = getOrganizationEmail(org.getId());

            String sendPulseId = getOrganizationEmailData("smtp_id", org.getId());
            String sendPulseKey = getOrganizationEmailData("smtp_key", org.getId());
            SendPulseService service = new SendPulseService(sendPulseId, sendPulseKey);

            Map<String,Object> variables = createUserWishlistEmailBody(org, user, items);
            String body = mailService.createBodyFromThymeleafTemplate(RESTOCKED_WISHLIST_TEMPLATE, variables);
            service.smtpSendMail(orgName, email, user.getName(), user.getEmail(),
                    body, "Items back in stock "+orgName, null);
        }
    }

    private Map<String, Object> createUserWishlistEmailBody(OrganizationEntity org, UserEntity user, List<WishlistMailItem> items) {
        String domain = domainService.getBackendUrl();
        String orgDomain = domainService.getOrganizationDomainAndSubDir(org.getId());
        String orgLogo = domain + "/files/"+ orderEmailHelper.getOrganizationLogo(org);
        String wishlistUrl = orgDomain + "/wishlist";
        String orgName = org.getName();
        String year = LocalDateTime.now().getYear()+"";

        Map<String, Object> params = new HashMap<>();
        params.put("orgDomain", orgDomain);
        params.put("domain", domain);
        params.put("orgName", orgName);
        params.put("orgLogo", orgLogo);
        params.put("year", year);
        params.put("wishlistUrl", wishlistUrl);
        params.put("userName", user.getName());
        params.put("items", items);

        return params;
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

    private List<WishlistItem> toCartItemsDto(List<WishlistItemEntity> cartItems) {
        return cartItems
                .stream()
                .map(this::createWishlistItemDto)
                .collect(toList());
    }

    private WishlistMailItem toWishlistMailItem(WishlistItemEntity entity) {
        String itemName = entity.getStock().getProductVariantsEntity().getName();
        Map<String, String> variantFeatures = productService.parseVariantFeatures(entity.getStock().getProductVariantsEntity(), 0);
        return new WishlistMailItem(itemName, entity.getCoverImage(), variantFeatures);
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

@Data
@AllArgsConstructor
class WishlistMailItem {
    private String name;
    private String coverImage;
    private Map<String, String> variantFeatures;
}
