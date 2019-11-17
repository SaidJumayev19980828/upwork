package com.nasnav.service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.CategoryDTO;
import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.OrganizationTagsRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.OrganizationTagsEntity;
import com.nasnav.persistence.TagGraphEdgesEntity;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.ResponseStatus;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private final BrandsRepository brandsRepository;

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private OrganizationTagsRepository orgTagsRepo;

    @Autowired
    private TagGraphEdgesRepository tagEdgesRepo;

    @Autowired
    public CategoryService(OrganizationRepository organizationRepository, BrandsRepository brandsRepository,
                           SocialRepository socialRepository, OrganizationThemeRepository organizationThemeRepository,
                           CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.brandsRepository = brandsRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public CategoryRepresentationObject getOrganizationCategories(Long organizationId) {

        List<BrandsEntity> brandsEntities = brandsRepository.findByOrganizationEntity_Id(organizationId);

        if(brandsEntities!=null && !brandsEntities.isEmpty()){
            List<String> categories = new ArrayList<>(brandsEntities.size());

//            brandsEntities.forEach(brandsEntity -> categories.add(brandsEntity.getCategories()));
        }

        //TODO brands table fix of categories Varchar [] to bigint []
        return null;
    }

    public List<CategoryRepresentationObject> getCategories(Long organizationId, Long categoryId){
        List<CategoriesEntity> categoriesEntityList = new ArrayList<>();
        CategoriesEntity categoriesEntity = null;
        List<CategoryRepresentationObject> categoriesList;
        if (organizationId == null && categoryId == null){
            categoriesEntityList = categoryRepository.findAll();
        }
        else if (categoryId == null) {
            List<Long> categoriesIdList = productRepository.getOrganizationCategoriesId(organizationId);
            categoriesEntityList = categoriesIdList.stream().map(id ->  categoryRepository.findById(id).get())
                    .collect(Collectors.toList());
        }
        else if (organizationId == null){
            if (categoryRepository.findById(categoryId).isPresent()) {
                categoriesEntity = categoryRepository.findById(categoryId).get();
                categoriesEntityList = categoryRepository.findByParentId(categoriesEntity.getId().intValue());
            }
        }
        else {
            //what to do if org_id and category_id exists ??
        }
        categoriesList = categoriesEntityList.stream().map(category -> (CategoryRepresentationObject) category.getRepresentation())
                .collect(Collectors.toList());
        if (categoriesEntity != null) {
            categoriesList.add((CategoryRepresentationObject)categoriesEntity.getRepresentation());
        }
        return categoriesList;
    }

    public ResponseEntity createCategory(CategoryDTO.CategoryModificationObject categoryJson) throws BusinessException {
        if (categoryJson.getName() == null) {
            throw new BusinessException("MISSING_PARAM: name", "No category name is provided", HttpStatus.NOT_ACCEPTABLE);
        } else if (!StringUtils.validateName(categoryJson.getName())) {
            throw new BusinessException(ResponseStatus.INVALID_PARAMETERS+": name",
                    "Provided name is not valid", HttpStatus.NOT_ACCEPTABLE);
        }
        CategoriesEntity categoriesEntity = new CategoriesEntity();
        categoriesEntity.setName(categoryJson.getName());
        categoriesEntity.setLogo(categoryJson.getLogo());
        if (categoryJson.getParentId() != null) {
            if (categoryRepository.findById(categoryJson.getParentId().longValue()).isPresent()) {
                categoriesEntity.setParentId(categoryJson.getParentId());
            } else {
                throw new BusinessException(ResponseStatus.INVALID_PARAMETERS+": parent_id",
                        "Provided parent category doesn't exit", HttpStatus.NOT_ACCEPTABLE);
            }
        }
        categoriesEntity.setPname(StringUtils.encodeUrl(categoryJson.getName()));
        categoriesEntity.setCreatedAt(LocalDateTime.now());
        categoriesEntity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(categoriesEntity);
        return new ResponseEntity(new CategoryResponse(categoriesEntity.getId()), HttpStatus.OK);
    }

    public ResponseEntity updateCategory(CategoryDTO.CategoryModificationObject categoryJson) throws BusinessException {
        if (categoryJson.getId() == null) {
            throw new BusinessException("MISSING_PARAM: ID", "No category ID is provided", HttpStatus.NOT_ACCEPTABLE);
        }
        if (!categoryRepository.findById(categoryJson.getId()).isPresent()){
            throw new BusinessException("EntityNotFound: category", "No category entity found with provided ID",
                    HttpStatus.NOT_ACCEPTABLE);
        }
        CategoriesEntity categoriesEntity = categoryRepository.findById(categoryJson.getId()).get();
        if (categoryJson.getName() != null) {
            if (StringUtils.validateName(categoryJson.getName())) {
            categoriesEntity.setName(categoryJson.getName());
            categoriesEntity.setPname(StringUtils.encodeUrl(categoryJson.getName()));
            } else {
                throw new BusinessException(ResponseStatus.INVALID_PARAMETERS+": name", "Provided name is not valid",
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
        if (categoryJson.getLogo() != null)
            categoriesEntity.setLogo(categoryJson.getLogo());
        if (categoryJson.getParentId() != null) {
            if (categoryRepository.findById(categoryJson.getParentId().longValue()).isPresent()) {
                categoriesEntity.setParentId(categoryJson.getParentId());
            } else {
                throw new BusinessException(ResponseStatus.INVALID_PARAMETERS+": parent_id",
                        "Provided parent category doesn't exit", HttpStatus.NOT_ACCEPTABLE);
            }
        }
        categoriesEntity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(categoriesEntity);
        return new ResponseEntity(new CategoryResponse(categoriesEntity.getId()), HttpStatus.OK);
    }

    public ResponseEntity deleteCategory(Long categoryId) throws BusinessException {
        if (!categoryRepository.findById(categoryId).isPresent()){
            throw new BusinessException("EntityNotFound: category",
                    "No category entity found with provided ID", HttpStatus.NOT_ACCEPTABLE);
        }
        CategoriesEntity categoriesEntity = categoryRepository.findById(categoryId).get();
        List<Long> productsIds = productRepository.getProductsByCategoryId(categoryId);
        if (productsIds.size() > 0){
            throw new BusinessException("NOT_EMPTY: products",
                    "There are still products "+productsIds.toString()+" assigned to this category", HttpStatus.CONFLICT);
        }
        List<Long> brandsIds = brandsRepository.getBrandsByCategoryId(categoryId.intValue());
        if (brandsIds.size() > 0){
            throw new BusinessException("NOT_EMPTY: brands",
                    "There are still brands "+brandsIds.toString()+" assigned to this category", HttpStatus.CONFLICT);
        }
        List<CategoriesEntity> childrenCategories = categoryRepository.findByParentId(categoryId.intValue());
        if (childrenCategories.size() > 0){
            List<Long> childrenCategoriesIds = childrenCategories.stream().map(category -> category.getId()).collect(Collectors.toList());
            throw new BusinessException("NOT_EMPTY: Category children ",
                    "There are still children " +childrenCategoriesIds+" assigned to this category", HttpStatus.CONFLICT);
        }
        categoryRepository.delete(categoriesEntity);
        return new ResponseEntity(new CategoryResponse(categoriesEntity.getId()),HttpStatus.OK);
    }

    public OrganizationTagsRepresentationObject getOrganizationTags(Long orgId) {
       /* List<OrganizationTagsEntity> orgTags = orgTagsRepo.findByOrganizationEntity_Id(orgId);
        //List<TagGraphEdgesEntity> tagsEdges = tagEdgesRepo.findByOrganizationEntity_Id(orgId);

        DirectedAcyclicGraph<OrganizationTagsRepresentationObject, DefaultEdge> tagsGraph =
                new DirectedAcyclicGraph<>(DefaultEdge.class);

        for(OrganizationTagsEntity i : orgTags)
            tagsGraph.addVertex( (OrganizationTagsRepresentationObject) i.getRepresentation());

        for(TagGraphEdgesEntity i : tagsEdges)
            tagsGraph.addEdge(i , i.getId());

        for(OrganizationTagsEntity i : orgTags)
            i.setChildren(Graphs.successorListOf(tagsGraph, i.getRepresentation()));
*/

        return null;
    }


}
