package com.nasnav.service.impl;

import com.nasnav.dao.*;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductFetchDTO;
import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.enumerations.PostStatus;
import com.nasnav.enumerations.PostType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PostLikesRepository postLikesRepository;
    @Autowired
    private PostClicksRepository postClicksRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowerServcie followerServcie;
    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Override
    public PostResponseDTO getPostById(long id) throws BusinessException {
        return fromEntityToPostResponseDto(postRepository.findById(id).orElseThrow(() -> new BusinessException("No Post with this id can be found", "", HttpStatus.NOT_FOUND)));
    }

    @Override
    public PostResponseDTO createPost(PostCreationDTO dto) throws BusinessException {
        return fromEntityToPostResponseDto(postRepository.save(fromPostCreationDtoToPostEntity(dto)));
    }

    @Override
    public Long likeOrDisLikePost(long postId, boolean likeAction) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$POST$0001, postId)
        );
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }

        PostLikesEntity found = postLikesRepository.getByUserAndPost((UserEntity) loggedInUser, postEntity);

        if (likeAction){
            if(found != null){
                throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,POST$LIKE$0003);
            }
            postLikesRepository.save(new PostLikesEntity(null,LocalDateTime.now(),(UserEntity) loggedInUser ,postEntity));
        }
        else {
            if(found != null) {
                postLikesRepository.delete(found);
            }
            else {
                throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, POST$LIKE$0002);
            }
        }
        return postLikesRepository.countAllByPost_Id(postId);
    }

    @Override
    public void clickOnPost(long postId) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$POST$0001));
        PostClicksEntity found = postClicksRepository.getByUserAndPost((UserEntity) loggedInUser, postEntity);
        if(found != null){
            //icrease number of clicks for the same user on the same post
            found.setClicksCount(found.getClicksCount() + 1);
            postClicksRepository.save(found);
        }
        else {
            //first time user click on post
            PostClicksEntity entity = new PostClicksEntity();
            entity.setPost(postEntity);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUser((UserEntity) loggedInUser);
            entity.setClicksCount(1);
            postClicksRepository.save(entity);
        }
    }

    @Override
    public void approveOrRejectReview(long postId, PostStatus postStatus) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof EmployeeUserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, G$USR$0001);
        }
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$POST$0001));
        postEntity.setStatus(postStatus.getValue());
        postRepository.save(postEntity);
    }

    @Override
    public PageImpl<PostResponseDTO> getHomeTimeLine(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        PageImpl<PostEntity> source = postRepository.getAllByUserInAndStatus(followerServcie.getAllFollowingAsUserEntity(loggedInUser.getId()), PostStatus.APPROVED.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());

    }

    @Override
    public PageImpl<PostResponseDTO> getUserTimeLine(long userId, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        userRepository.findById(userId).orElseThrow(()->new RuntimeBusinessException(HttpStatus.NOT_FOUND,E$USR$0001));
        PageImpl<PostEntity> source = postRepository.getAllByUser_IdAndStatus(userId, PostStatus.APPROVED.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<PostResponseDTO> getUserPendingPosts(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        PageImpl<PostEntity> source = postRepository.getAllByUser_IdAndStatus(loggedInUser.getId(), PostStatus.PENDING.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<PostResponseDTO> getOrgReviews(PostStatus postStatus, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof EmployeeUserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, G$USR$0001);
        }
        organizationRepository.findById(loggedInUser.getOrganizationId())
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, loggedInUser.getOrganizationId()));

        if (postStatus != null){
            PageImpl<PostEntity> source = postRepository.getAllByOrganization_IdAndStatusAndType(loggedInUser.getOrganizationId(), postStatus.getValue(), PostType.REVIEW.getValue(), page);
            List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
            return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
        }
        PageImpl<PostEntity> source = postRepository.getAllByOrganization_IdAndType(loggedInUser.getOrganizationId(), PostType.REVIEW.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<PostResponseDTO> getOrgSharedProducts(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof EmployeeUserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, G$USR$0001);
        }
        organizationRepository.findById(loggedInUser.getOrganizationId())
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, loggedInUser.getOrganizationId()));

        PageImpl<PostEntity> source = postRepository.getAllByOrganization_IdAndType(loggedInUser.getOrganizationId(), PostType.POST.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());

    }

   @Override
    public PageImpl<PostEntity> getAllPostsWithinAdvertisement(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
       return postRepository.findAllByAdvertisementIsNotNullAndAdvertisement_FromDateLessThanEqualAndAdvertisement_ToDateGreaterThanEqual(LocalDateTime.now(), LocalDateTime.now(), page);
    }

    @Override
    public void saveForLater(Long postId) {
        BaseUserEntity currentUser = securityService.getCurrentUser();
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$POST$0001));
            UserEntity user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,U$0001));
                post.getSavedByUsers().add(user);
                postRepository.save(post);


    }


    @Override
    public void unSavePost(Long postId) {
        BaseUserEntity currentUser = securityService.getCurrentUser();
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$POST$0001));
        UserEntity user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,U$0001));
            post.getSavedByUsers().remove(user);
            postRepository.save(post);

    }

    @Override
    public PageImpl<PostResponseDTO> getUserSavedPosts(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,U$0001));

        PageImpl<PostEntity> source = postRepository.getAllBySavedByUsersContains(user, page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());

    }


    private PostEntity fromPostCreationDtoToPostEntity(PostCreationDTO dto) {
        List<ProductEntity> products = new ArrayList<>();
        OrganizationEntity org = organizationRepository.findById(dto.getOrganizationId()).orElseThrow(
                () -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, dto.getOrganizationId())
        );
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        if (!dto.getIsReview()) {
            dto.getProductsIds().forEach(i -> {
                Optional<ProductEntity> product = productRepository.findByIdAndOrganizationId(i, org.getId());
                if (product.isPresent() && Arrays.asList(ProductTypes.STOCK_ITEM, ProductTypes.BUNDLE).contains(product.get().getProductType())) {
                    products.add(product.get());
                } else {
                    throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, P$PRO$0016, i);
                }
            });
        }
        PostEntity entity = new PostEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDescription(dto.getDescription());
        entity.setOrganization(org);
        entity.setUser((UserEntity) loggedInUser);
        entity.setProducts(products);
        entity.setStatus(PostStatus.APPROVED.getValue());
        entity.setType(PostType.POST.getValue());
        if(dto.getIsReview()) {
            if(dto.getAttachments() == null || dto.getAttachments().size() == 0){
                throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,POST$REVIEW$ATTACHMENT);
            }
            entity.setStatus(PostStatus.PENDING.getValue());
            entity.setType(PostType.REVIEW.getValue());
            entity.setAttachments(dto.getAttachments());
            dto.getAttachments().forEach(o -> {
                o.setPost(entity);
            });
        }
        if (dto.getAdvertisementId() != null) {
            Assert.notNull(entity.getUser().getBankAccount(), "user should create bank account first");
            entity.setAdvertisement(advertisementRepository.getOne(dto.getAdvertisementId()));
        }

        return entity;
    }

    private PostResponseDTO fromEntityToPostResponseDto(PostEntity entity) {
        ProductFetchDTO productFetchDTO = new ProductFetchDTO();
        productFetchDTO.setCheckVariants(false);
        productFetchDTO.setIncludeOutOfStock(true);
        productFetchDTO.setOnlyYeshteryProducts(false);
        Set<ProductDetailsDTO> productDetailsDTOS = new HashSet<>();
        entity.getProducts().forEach(o -> {
            try {
                productFetchDTO.setProductId(o.getId());
                productDetailsDTOS.add(productService.getProduct(productFetchDTO));
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        });
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDescription(entity.getDescription());
        dto.setStatus(PostStatus.getEnumByValue(entity.getStatus()));
        dto.setType(PostType.getEnumByValue(entity.getType()));
        dto.setAttachments(entity.getAttachments());
        dto.setLikesCount(postLikesRepository.countAllByPost_Id(entity.getId()));
        dto.setClicksCount(postClicksRepository.getClicksCountByPost(entity.getId()));
        dto.setProducts(productDetailsDTOS);
        dto.setOrganization(organizationService.getOrganizationById(entity.getOrganization().getId(),0));
        dto.setUser(entity.getUser().getRepresentation());

        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (loggedInUser instanceof UserEntity) {
            dto.setIsLiked(postLikesRepository.existsByUser_IdAndPost_Id(loggedInUser.getId(), entity.getId()));
            dto.setIsSaved(userSaveThatPost (entity.getSavedByUsers(),loggedInUser.getId()));

        }
        else {
            dto.setIsLiked(false);
            dto.setIsSaved(false);
        }

        return dto;
    }


    private Boolean userSaveThatPost(Set<UserEntity> entity , Long userId) {
        return entity.stream().filter(user -> user.getId().equals(userId)).findAny().isPresent();
    }

}
