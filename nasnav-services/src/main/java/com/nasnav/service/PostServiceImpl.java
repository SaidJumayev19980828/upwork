package com.nasnav.service;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dao.PostRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductFetchDTO;
import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.GeneralRepresentationDto;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
    private ProductService productService;
    @Autowired
    private OrganizationService organizationService;

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

    private PostEntity fromPostCreationDtoToPostEntity(PostCreationDTO dto) {
        List<ProductEntity> products = new ArrayList<>();
        OrganizationEntity org = organizationRepository.findById(dto.getOrganizationId()).orElseThrow(
                () -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, dto.getOrganizationId())
        );
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (!(loggedInUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, E$USR$0001);
        }
        dto.getProductsIds().forEach(i -> {
            Optional<ProductEntity> product = productRepository.findByIdAndOrganizationId(i, org.getId());
            if (product.isPresent()) {
                products.add(product.get());
            }
        });
        PostEntity entity = new PostEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDescription(dto.getDescription());
        entity.setAttachments(dto.getAttachments());
        entity.setOrganization(org);
        entity.setUser((UserEntity) loggedInUser);
        entity.setProducts(products);
        dto.getAttachments().forEach(o -> {
            o.setPost(entity);
        });

        return entity;
    }

    private PostResponseDTO fromEntityToPostResponseDto(PostEntity entity) throws BusinessException {
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
        dto.setDescription(entity.getDescription());
        dto.setAttachments(entity.getAttachments());
        dto.setLikesCount(postLikesRepository.countAllByPost_Id(entity.getId()));
        dto.setProducts(productDetailsDTOS);
        dto.setOrganization(organizationService.getOrganizationById(entity.getOrganization().getId(),0));
        dto.setUser(new GeneralRepresentationDto(entity.getUser().getId(), entity.getUser().getFirstName()));

        return dto;
    }


}
