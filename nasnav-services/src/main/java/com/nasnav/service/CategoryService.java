package com.nasnav.service;

import java.util.List;

import com.nasnav.dto.*;


import com.nasnav.dto.response.CategoryDto;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.TagResponse;

public interface CategoryService {

  long DUMMY_ROOT_PARENT = -1L;

  TagsRepresentationObject getTagById(Long tagId) throws BusinessException;

  //    @CacheResult(cacheName = "organizations_categories")
  List<CategoryRepresentationObject> getCategories(Long organizationId, Long categoryId);

  CategoryResponse addOrUpdateCategory(CategoryDTO categoryDTO);

  CategoriesEntity createCategory(CategoryDTO categoryJson);

  CategoriesEntity updateCategory(CategoryDTO categoryJson);

  CategoryResponse deleteCategory(Long categoryId) throws BusinessException;

  //    @CacheResult(cacheName = "organizations_tags")
  List<TagsRepresentationObject> getOrganizationTags(Long orgId, String categoryName);

  PaginatedResponse<TagsRepresentationObject> getOrganizationTagsPageable(Long orgId, String categoryName, Integer start, Integer count);

  PaginatedResponse<TagsRepresentationObject> getYeshteryOrganizationsTags(Integer start, Integer count , String categoryName, Long orgId);

  List<TagsTreeNodeDTO> getOrganizationTagsTree(Long orgId) throws BusinessException;

  TagResponse createOrUpdateTagThroughApi(TagsDTO tagDTO) throws BusinessException;

  TagsEntity createOrUpdateTag(TagsDTO tagDTO) throws BusinessException;

  void createTagTree(TagsTreeCreationDTO tagsTreeDTO) throws BusinessException;

  TagResponse deleteOrgTag(Long tagId) throws BusinessException;

  void assignTagsCategory(Long categortId, List<Long> tagsIds) throws BusinessException;

  void setTagsListCategory(UpdateTagsCategoryDTO updateDto);

  void setProductsListCategory(UpdateProductsCategoryDTO updateDto);

  List<TagsRepresentationObject> findCollections(String name, Long orgId);

  List<CategoryDto> getCategoriesTree();

  List<TagsRepresentationObject> getYeshteryTags(String categoryName);

  List<TagsTreeNodeDTO> getYeshteryTagsTree() throws BusinessException;

}