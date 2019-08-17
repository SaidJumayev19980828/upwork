package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final BrandsRepository brandsRepository;

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final ProductRepository productRepository;

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
        List<CategoriesEntity> categoriesEntityList = null;
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
            Long parentId = categoryRepository.findById(categoryId).get().getId();
            categoriesEntityList = categoryRepository.findByParentId(parentId.intValue());
        }
        else {
            //what to do if org_id and category_id exists ??
        }
        categoriesList = categoriesEntityList.stream().map(category -> (CategoryRepresentationObject) category.getRepresentation())
                .collect(Collectors.toList());
        if (categoriesEntity != null) {
            categoriesList.add((CategoryRepresentationObject)categoriesEntity.getRepresentation());
        }
        return  categoriesList;
    }

    public ApiResponseBuilder createCategory(CategoryRepresentationObject categoryJson) {
        CategoriesEntity categoriesEntity = new CategoriesEntity();
        if (categoryJson.getName() == null || !EntityUtils.validateName(categoryJson.getName())) {
            //return new ApiResponseBuilder().setSuccess(false).setResponseStatuses(Collections.singletonList(ResponseStatus.INVALID_NAME)).build();
        }
        categoriesEntity.setName(categoryJson.getName());
        categoriesEntity.setLogo(categoryJson.getLogoUrl());
        categoriesEntity.setParentId(categoryJson.getParentId());
        categoryRepository.save(categoriesEntity);
        return new ApiResponseBuilder().setSuccess(true).setEntityId(categoriesEntity.getId()).build();
    }

    public ApiResponseBuilder updateCategory(CategoryRepresentationObject categoryJson) {
        if (categoryRepository.findById(categoryJson.getId()) != null){
            return new ApiResponseBuilder().setSuccess(false).setEntityId(categoryJson.getId()).setMessage("").build();
        }
    }
}
