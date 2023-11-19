package com.nasnav.service;

import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;
import static com.nasnav.cache.Caches.ORGANIZATIONS_TAG_TREES;

import java.util.List;

import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dto.CategoryDTO;
import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.TagsDTO;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.TagsTreeCreationDTO;
import com.nasnav.dto.TagsTreeNodeDTO;
import com.nasnav.dto.UpdateProductsCategoryDTO;
import com.nasnav.dto.UpdateTagsCategoryDTO;
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

  PageImpl<TagsRepresentationObject> getYeshteryOrganizationsTags(Integer start, Integer count , String categoryName, Long orgId);

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