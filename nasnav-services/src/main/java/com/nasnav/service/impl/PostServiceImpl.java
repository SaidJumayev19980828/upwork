package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
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
import com.nasnav.request.ImageBase64;
import com.nasnav.service.*;
import com.nasnav.util.MultipartFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final ProductRepository productRepository;
    private final PostLikesRepository postLikesRepository;
    private final PostClicksRepository postClicksRepository;
    private  final ProductService productService;
    private  final OrganizationService organizationService;
    private  final UserRepository userRepository;
    private final FollowerServcie followerServcie;
    private final AdvertisementRepository advertisementRepository;
    private final  FileService fileService;
    private final ShopService shopService;

    @Override
    public PostResponseDTO getPostById(long id) throws BusinessException {
        return fromEntityToPostResponseDto(postRepository.findById(id).orElseThrow(() -> new BusinessException("No Post with this id can be found", "", HttpStatus.NOT_FOUND)));
    }

    @Override
    public PostResponseDTO createPost(PostCreationDTO dto) throws BusinessException, IOException {
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
    public PageImpl<PostResponseDTO> getFilterForUser(long userId, Integer start, Integer count, String type)
    {
        PageRequest page = getQueryPage(start, count);
        userRepository.findById(userId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, E$USR$0001));
        PageImpl<PostEntity> source = null;
        List<PostResponseDTO> dtos = new ArrayList<>();
        if ("explore".equals(type) || type == null) {
            source = postRepository.getAllByUser_IdAndStatus(userId, PostStatus.APPROVED.getValue(), page);
        }
        else if ("reviews".equals(type)) {
            source = postRepository.getAllByUser_IdAndType(userId, PostType.REVIEW.getValue(), page);
        }
        else if ("following".equals(type)) {
            source = postRepository.getAllByUser_IdAndType(userId, PostType.POST.getValue(), page);
        }
        if (source != null){
            dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).collect(Collectors.toList());
            return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
        }
        return new PageImpl<>(dtos);
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
        Pageable page = new CustomPaginationPageRequest(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,U$0001));

        PageImpl<PostEntity> source = postRepository.getAllBySavedByUsersContains(user, page);
        List<PostResponseDTO> postResponseList = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
        return new PageImpl<>(postResponseList, source.getPageable(), source.getTotalElements());

    }

    private PostEntity fromPostCreationDtoToPostEntity(PostCreationDTO dto) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        List<ProductEntity> products = getProducts(dto);
        OrganizationEntity org = getOrganization(dto.getOrganizationId());

        PostEntity entity = buildPostEntity(dto, org, (UserEntity) loggedInUser);

        List<SubPostEntity> subPosts = createSubPosts(products);
        subPosts.forEach(entity::addSubPost);

        if (dto.isReview()) {
            handleReviewAttachments(dto, org, entity);
        }

        handleAdvertisement(dto, entity);

        return entity;
    }

    private List<SubPostEntity> createSubPosts(List<ProductEntity> products) {
        return products.stream()
                .map(this::createSubPost)
                .toList();
    }

    private SubPostEntity createSubPost(ProductEntity product) {
        SubPostEntity subPost = new SubPostEntity();
        subPost.setProduct(product);
        return subPost;
    }

    private List<ProductEntity> getProducts(PostCreationDTO dto) {
        List<ProductEntity> products = new ArrayList<>();
        OrganizationEntity org = getOrganization(dto.getOrganizationId());

        if (!dto.isReview()) {
            dto.getProductsIds().forEach(productId -> {
                Optional<ProductEntity> product = getProductByIdAndOrganization(productId, org.getId());
                validateAndAddProduct(products, product);
            });
        }

        return products;
    }

    private OrganizationEntity getOrganization(Long organizationId) {
        if (organizationId!=null)
            return organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, organizationId));
       return securityService.getCurrentUserOrganization();
    }


    private Optional<ProductEntity> getProductByIdAndOrganization(Long productId, Long orgId) {
        return productRepository.findByIdAndOrganizationId(productId, orgId);
    }

    private void validateAndAddProduct(List<ProductEntity> products, Optional<ProductEntity> product) {
        if (product.isPresent() && Arrays.asList(ProductTypes.STOCK_ITEM, ProductTypes.BUNDLE).contains(product.get().getProductType())) {
            products.add(product.get());
        } else {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, P$PRO$0016, product.map(ProductEntity::getId).orElse(null));
        }
    }

    /**
     *   This method is deprecated because dealing with posts changed to create sub post for each Product
     */
    @Deprecated(since = "21/3", forRemoval = true)
    private PostEntity buildPostEntity(PostCreationDTO dto, List<ProductEntity> products, OrganizationEntity org, UserEntity loggedInUser) {
        PostEntity entity = new PostEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDescription(dto.getDescription());
        entity.setOrganization(org);
        entity.setUser(loggedInUser);
        entity.setProducts(products);
        entity.setStatus(PostStatus.APPROVED.getValue());
        entity.setType(PostType.POST.getValue());
        return entity;
    }


    private PostEntity buildPostEntity(PostCreationDTO dto, OrganizationEntity org, UserEntity loggedInUser) {
        PostEntity entity = new PostEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDescription(dto.getDescription());
        entity.setOrganization(org);
        entity.setUser(loggedInUser);
        entity.setStatus(PostStatus.APPROVED.getValue());
        entity.setType(PostType.POST.getValue());
        return entity;
    }
    private void handleReviewAttachments(PostCreationDTO dto, OrganizationEntity org, PostEntity post) {
        if (dto.getAttachment()!= null && dto.getAttachment().isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, POST$REVIEW$ATTACHMENT);
        }
        post.setProductName(dto.getProductName());
        post.setRating(dto.getRating());
        post.setShop(shopService.shopById(dto.getShopId()));
        post.setStatus(PostStatus.PENDING.getValue());
        post.setType(PostType.REVIEW.getValue());
        createAttachmentEntities(dto.getAttachment(),org,post);
    }
    private void createAttachmentEntities(List<ImageBase64> attachments, OrganizationEntity org, PostEntity entity) {
         attachments.forEach(attachment -> {
                    try {
                         createAttachmentEntity(attachment, org, entity);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
         });
    }

    private void createAttachmentEntity(ImageBase64 attachment, OrganizationEntity org, PostEntity entity) throws IOException {
        String attachmentUrl = uploadPostAttachment(attachment, org.getId());
        PostAttachmentsEntity attachmentEntity = PostAttachmentsEntity.buildAttachment(attachmentUrl, String.valueOf(PostType.REVIEW));
        entity.addAttachment(attachmentEntity);
    }



    private void handleAdvertisement(PostCreationDTO dto, PostEntity entity) {
        if (dto.getAdvertisementId() != null) {
            Assert.notNull(entity.getUser().getBankAccount(), "user should create bank account first");
            entity.setAdvertisement(advertisementRepository.getOne(dto.getAdvertisementId()));
        }
    }

    public String uploadPostAttachment(ImageBase64 attachment , Long orgId) throws IOException {
        MultipartFile attachmentFile = MultipartFileUtils.convert(attachment.getBase64(), attachment.getFileName(), attachment.getFileType());
        return fileService.saveFile(attachmentFile ,orgId );
    }
    private PostResponseDTO fromEntityToPostResponseDto(PostEntity entity) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setSubPosts(entity.getSubPosts());
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDescription(entity.getDescription());
        dto.setStatus(PostStatus.getEnumByValue(entity.getStatus()));
        dto.setType(PostType.getEnumByValue(entity.getType()));
        dto.setAttachments(entity.getAttachments());
        dto.setLikesCount(postLikesRepository.countAllByPost_Id(entity.getId()));
        dto.setClicksCount(postClicksRepository.getClicksCountByPost(entity.getId()));

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
    if (dto.getType().equals(PostType.REVIEW)) {
        dto.setRating(entity.getRating());
        if (entity.getShop() != null) {
            dto.setShop(entity.getShop().getRepresentation());
        }
        dto.setProductName(entity.getProductName());
    }
        return dto;
    }


    private Boolean userSaveThatPost(Set<UserEntity> entity , Long userId) {
        return entity.stream().anyMatch(user -> user.getId().equals(userId));
    }


    /**
     * This method is deprecated because dealing with posts changed to create sub post for each Product
     * @deprecated
     * @param entity
     */
    @Deprecated (since = "21/3", forRemoval = true)
    private void setProduct(PostEntity entity){
        ProductFetchDTO productFetchDTO = new ProductFetchDTO();
        productFetchDTO.setCheckVariants(false);
        productFetchDTO.setIncludeOutOfStock(true);
        productFetchDTO.setOnlyYeshteryProducts(false);
        Set<ProductDetailsDTO> productDetailsDTOs = new HashSet<>();
        entity.getProducts().forEach(o -> {
            try {
                productFetchDTO.setProductId(o.getId());
                productDetailsDTOs.add(productService.getProduct(productFetchDTO));
            } catch (BusinessException e) {
                log.error(e.getErrorMessage());
            }
        });

    }
}
