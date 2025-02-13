package com.nasnav.service.impl;

import com.nasnav.dao.*;
import com.nasnav.dto.request.product.ProductRateDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.ReviewService;
import com.nasnav.service.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;

@Service
public class ReviewServiceImpl implements ReviewService{

    @Autowired
    private SecurityService securityService;
    @Autowired
    private ProductRatingRepository productRatingRepo;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private OrdersRepository ordersRepository;

    @Override
    public void rateProduct(ProductRateDTO dto) {
        BaseUserEntity baseUser = securityService.getCurrentUser();
        if (baseUser instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, E$USR$0001);
        }
        UserEntity user = (UserEntity) baseUser;
        validateProductRateDTO(dto, user.getId());
        ProductVariantsEntity variant = productVariantsRepository.findById(dto.getVariantId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$VAR$0001, dto.getVariantId()));
        createProductRate(dto, variant, user);
    }


    private void createProductRate(ProductRateDTO dto, ProductVariantsEntity variant, UserEntity user) {
        ProductRating rate = productRatingRepo.findByVariant_IdAndUser_Id(variant.getId(), user.getId())
                .orElse(new ProductRating());
        rate.setRate(dto.getRate());
        rate.setVariant(variant);
        rate.setReview(dto.getReview());
        rate.setUser(user);
        rate.setApproved(false);
        productRatingRepo.save(rate);
    }


    private void validateProductRateDTO(ProductRateDTO dto, Long userId) {
        if (anyIsNull(dto.getVariantId(), dto.getRate(), dto.getOrderId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$004 );
        }
        if (dto.getRate() > 5 || dto.getRate() < 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$006 );
        }
        if (ordersRepository.getStoreConfirmedOrderCountPerUser(dto.getOrderId(), userId) == 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$007);
        }
    }


    @Override
    public void approveRate(Long rateId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        ProductRating rate = productRatingRepo.findByIdAndVariant_ProductEntity_OrganizationId(rateId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$VAR$005, rateId));
        rate.setApproved(true);
        productRatingRepo.save(rate);
    }


    @Override
    public List<ProductRateRepresentationObject> getVariantRatings(Long variantId) {
        return toReviewDtoList(productRatingRepo.findApprovedVariantRatings(variantId));
    }

    @Override
    public List<ProductRateRepresentationObject> getProductRatings(Long productId) {
        return toReviewDtoList(productRatingRepo.findApprovedProductRatings(productId));
    }

    @Override
    public List<ProductRateRepresentationObject> getYeshteryVariantRatings(Long variantId) {
        return toReviewDtoList(productRatingRepo.findApprovedYeshteryVariantRatings(variantId));
    }

    @Override
    public List<ProductRateRepresentationObject> getYeshteryProductRatings(Long productId) {
        return toReviewDtoList(productRatingRepo.findApprovedYeshteryProductRatings(productId));
    }


    @Override
    public List<ProductRateRepresentationObject> getProductsRatings() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return toReviewDtoList(productRatingRepo.findUnapprovedVariantsRatings(orgId));
    }

    private List<ProductRateRepresentationObject> toReviewDtoList(List<ProductRating> ratings) {
        return ratings
                .stream()
                .map(rating ->(ProductRateRepresentationObject) rating.getRepresentation())
                .collect(toList());
    }

    @Override
    public List<ProductRateRepresentationObject> getUserProductsRatings(Set<Long> variantIds) {
        Long userId  = securityService.getCurrentUser().getId();
        return toReviewDtoList(productRatingRepo.findUserVariantRatings(userId, variantIds));
    }
}
