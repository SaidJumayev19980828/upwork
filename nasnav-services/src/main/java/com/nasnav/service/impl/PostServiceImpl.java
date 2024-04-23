package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.PostClicksRepository;
import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dao.PostRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.SubPostEntityRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductFetchDTO;
import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.dto.response.SubPostResponseDTO;
import com.nasnav.enumerations.PostStatus;
import com.nasnav.enumerations.PostType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PostAttachmentsEntity;
import com.nasnav.persistence.PostClicksEntity;
import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.PostLikesEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.SubPostEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.request.ImageBase64;
import com.nasnav.service.FileService;
import com.nasnav.service.FollowerServcie;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.PostService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ShopService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.ADVER$001;
import static com.nasnav.exceptions.ErrorCodes.E$USR$0001;
import static com.nasnav.exceptions.ErrorCodes.G$ORG$0001;
import static com.nasnav.exceptions.ErrorCodes.G$POST$0001;
import static com.nasnav.exceptions.ErrorCodes.G$USR$0001;
import static com.nasnav.exceptions.ErrorCodes.GLOBAL;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0000;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0016;
import static com.nasnav.exceptions.ErrorCodes.POST$REVIEW$ATTACHMENT;
import static com.nasnav.exceptions.ErrorCodes.U$0001;

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
    private final ProductService productService;
    private final OrganizationService organizationService;
    private final UserRepository userRepository;
    private final FollowerServcie followerServcie;
    private final AdvertisementRepository advertisementRepository;
    private final  FileService fileService;
    private final ShopService shopService;
    private final SubPostEntityRepository subPostRepository;
    @Override
    public PostResponseDTO getPostById(long id) throws BusinessException {
        return fromEntityToPostResponseDto(postRepository.findById(id).orElseThrow(() -> new BusinessException("No Post with this id can be found", "", HttpStatus.NOT_FOUND)));
    }

    @Override
    public PostResponseDTO createPost(PostCreationDTO dto) throws BusinessException, IOException {
        return fromEntityToPostResponseDto(postRepository.save(fromPostCreationDtoToPostEntity(dto)));
    }

    @Override
    public long likeOrDisLikePost(long postId, boolean likeAction) {
        SubPostEntity post = subPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$POST$0001, postId));
        UserEntity loggedInUser = validateUser(securityService.getCurrentUser());
        return  subPostRepository.save(toggleLike(post, loggedInUser)).getLikes().size();
    }

    private UserEntity validateUser(BaseUserEntity loggedIn) {
        if (!(loggedIn instanceof UserEntity user)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        return user;
    }


    private SubPostEntity toggleLike(SubPostEntity subPost, UserEntity user) {
        Set<PostLikesEntity> likes = subPost.getLikes();
        boolean userLikedPost = likes.stream()
                .anyMatch(like -> like.getUser().equals(user));
        if (userLikedPost) {
            likes.removeIf(like -> like.getUser().equals(user));
        } else {
           subPost.addLike(buildLike(user));
        }
        return subPost;
    }

    private PostLikesEntity buildLike(UserEntity user){
        PostLikesEntity newLike = new PostLikesEntity();
        newLike.setUser(user);
        newLike.setCreatedAt(LocalDateTime.now());
        return newLike;
    }

    @Override
    public void clickOnPost(long postId) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        UserEntity user = validateUser(loggedInUser);
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$POST$0001));
        PostClicksEntity found = postClicksRepository.getByUserAndPost(user, postEntity);
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
            entity.setUser(user);
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
        UserEntity user = validateUser(loggedInUser);
        PageImpl<PostEntity> source = postRepository.getAllByUserInAndStatus(followerServcie.getAllFollowingAsUserEntity(user.getId()), PostStatus.APPROVED.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());

    }

    @Override
    public PageImpl<PostResponseDTO> getUserTimeLine(long userId, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
       UserEntity user = userRepository.findById(userId).orElseThrow(()->new RuntimeBusinessException(HttpStatus.NOT_FOUND,E$USR$0001));
        PageImpl<PostEntity> source = postRepository.getAllByUserAndStatus(user, PostStatus.APPROVED.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<PostResponseDTO> getFilterForUser(long userId, Integer start, Integer count, String type)
    {
        PageRequest page = getQueryPage(start, count);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, E$USR$0001));
        PageImpl<PostEntity> source = null;
        List<PostResponseDTO> dtos = new ArrayList<>();
        if ("explore".equals(type) || type == null) {
            source = postRepository.getAllByUserAndStatus(user, PostStatus.APPROVED.getValue(), page);
        }
        else if ("reviews".equals(type)) {
            source = postRepository.getAllByUser_IdAndType(userId, PostType.REVIEW.getValue(), page);
        }
        else if ("following".equals(type)) {
            source = postRepository.getAllByUser_IdAndType(userId, PostType.POST.getValue(), page);
        }
        if (source != null){
            dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
            return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
        }
        return new PageImpl<>(dtos);
    }

    @Override
    public PageImpl<PostResponseDTO> getUserPendingPosts(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        UserEntity user = validateUser(loggedInUser);
        PageImpl<PostEntity> source = postRepository.getAllByUserAndStatus(user, PostStatus.PENDING.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<PostResponseDTO> getOrgReviews(PostStatus postStatus, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof EmployeeUserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, G$USR$0001);
        }
       OrganizationEntity organization=  organizationRepository.findById(loggedInUser.getOrganizationId())
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, loggedInUser.getOrganizationId()));

        if (postStatus != null){
            PageImpl<PostEntity> source = postRepository.getAllByOrganization_IdAndStatusAndType(organization.getId(), postStatus.getValue(), PostType.REVIEW.getValue(), page);
            List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
            return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
        }
        PageImpl<PostEntity> source = postRepository.getAllByOrganization_IdAndType(loggedInUser.getOrganizationId(), PostType.REVIEW.getValue(), page);
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
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
        List<PostResponseDTO> dtos = source.getContent().stream().map(this::fromEntityToPostResponseDto).toList();
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
                validateAndAddProduct(products,  getProductByIdAndOrganization(productId, org.getId()));
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


    private ProductEntity getProductByIdAndOrganization(Long productId, Long orgId) {
        return productRepository.findByIdAndOrganizationId(productId, orgId).orElseThrow(()->
                new RuntimeBusinessException(HttpStatus.NOT_FOUND,P$PRO$0000,productId)
                );
    }

    private void validateAndAddProduct(List<ProductEntity> products, ProductEntity product) {
        if (isValidProductType(product.getProductType())) {
            products.add(product);
        } else {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, P$PRO$0016, product.getId());
        }
    }

    private boolean isValidProductType(int productType) {
        return Arrays.asList(ProductTypes.STOCK_ITEM, ProductTypes.BUNDLE).contains(productType);
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
                        throw new RuntimeBusinessException(HttpStatus.INTERNAL_SERVER_ERROR,GLOBAL,e.getMessage());
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
            entity.setAdvertisement(advertisement(dto.getAdvertisementId()));
        }
    }

    private AdvertisementEntity advertisement(long advertisementId) {
          return  advertisementRepository.findById(advertisementId)
                    .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ADVER$001, advertisementId));
    }

    private String uploadPostAttachment(ImageBase64 attachment , Long orgId) throws IOException {
        MultipartFile attachmentFile = MultipartFileUtils.convert(attachment.getBase64(), attachment.getFileName(), attachment.getFileType());
        return fileService.saveFile(attachmentFile ,orgId );
    }
    private PostResponseDTO fromEntityToPostResponseDto(PostEntity entity) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDescription(entity.getDescription());
        dto.setStatus(PostStatus.getEnumByValue(entity.getStatus()));
        dto.setType(PostType.getEnumByValue(entity.getType()));
        dto.setAttachments(entity.getAttachments());
        dto.setClicksCount(postClicksRepository.getClicksCountByPost(entity.getId()));
        dto.setOrganization(organizationService.getOrganizationById(entity.getOrganization().getId(),0));
        dto.setUser(entity.getUser().getRepresentation());
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        dto.setSubPosts( entity.getSubPosts()
                .stream().map(sub-> buildSubPostResponseDto(entity.getId(),sub,loggedInUser))
                .collect(Collectors.toSet())
        );

        if (loggedInUser instanceof UserEntity) {
            dto.setIsSaved(userSaveThatPost (entity.getSavedByUsers(),loggedInUser.getId()));
        } else {
            dto.setIsSaved(false);
        }
        buildReview(entity, dto);
        return dto;
    }

    private SubPostResponseDTO buildSubPostResponseDto(Long parentId , SubPostEntity entity , BaseUserEntity user) {
        SubPostResponseDTO dto = new SubPostResponseDTO();
        dto.setId(entity.getId());
        dto.setParentPostId(parentId);
        dto.setProduct(buildProductFetchDto(entity));
        dto.setLikesCount(likesCount(entity));
        dto.setLiked(userLikeThePost(entity, user));
        return dto;
    }

    private ProductDetailsDTO buildProductFetchDto(SubPostEntity entity){
        ProductFetchDTO dto = new ProductFetchDTO();
        dto.setCheckVariants(false);
        dto.setIncludeOutOfStock(true);
        dto.setOnlyYeshteryProducts(false);
        dto.setProductId(entity.getProduct().getId());
        return productService.getProduct(dto);
    }

    private long likesCount(SubPostEntity subPost) {
        return subPost.getLikes().size();
    }
    private boolean userLikeThePost(SubPostEntity subPost, BaseUserEntity loggedInUser) {
        if (loggedInUser instanceof UserEntity user)
            return subPost.getLikes().stream().anyMatch(post-> post.getUser().equals(user));
        else
            return false;
    }

    private void buildReview(PostEntity entity , PostResponseDTO dto){
        if (dto.getType().equals(PostType.REVIEW)) {
            dto.setRating(entity.getRating());
            if (entity.getShop() != null) {
                dto.setShop(entity.getShop().getRepresentation());
            }
            dto.setProductName(entity.getProductName());
        }
    }


    private Boolean userSaveThatPost(Set<UserEntity> entity , Long userId) {
        return entity.stream().anyMatch(user -> user.getId().equals(userId));
    }

}
