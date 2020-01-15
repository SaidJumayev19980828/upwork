package com.nasnav.service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.ResponseStatus;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
    private TagsRepository orgTagsRepo;

    @Autowired
    private TagGraphEdgesRepository tagEdgesRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private OrganizationRepository orgRepo;

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

    public List<CategoryRepresentationObject> getCategories(Long categoryId){
        List<CategoriesEntity> categoriesEntityList = new ArrayList<>();
        CategoriesEntity categoriesEntity = null;
        List<CategoryRepresentationObject> categoriesList;
        if (categoryId == null)
            categoriesEntityList = categoryRepository.findAll();
        else {
            if (categoryRepository.findById(categoryId).isPresent()) {
                categoriesEntity = categoryRepository.findById(categoryId).get();
                categoriesEntityList = categoryRepository.findByParentId(categoriesEntity.getId().intValue());
            }
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
        List<Long> productsIds = new ArrayList<>();
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






    public List<TagsRepresentationObject> getOrganizationTags(Long orgId) {
        return orgTagsRepo.findByOrganizationEntity_Id(orgId)
                .stream()
                .map(tag ->(TagsRepresentationObject) tag.getRepresentation())
                .collect(Collectors.toList());
    }

    public List<TagsRepresentationObject> getOrganizationTagsTree(Long orgId) {

        List<TagsRepresentationObject> orgTags = orgTagsRepo.getTagsByOrgId(orgRepo.getOne(orgId))
                .stream().map(tag ->(TagsRepresentationObject) tag.getRepresentation())
                .collect(Collectors.toList());;

        List<TagGraphEdgesEntity> edges = tagEdgesRepo.findByChildIdIn(orgTags.stream().map(tag -> tag.getId()).collect(Collectors.toSet()));

        List <TagsRepresentationObject> removedChildren = new ArrayList<>();

        for(TagsRepresentationObject entity : orgTags){
            if(!tagEdgesRepo.findByParentIdIsNotNullAndChildId(entity.getId()).isEmpty()) {

                List<TagsRepresentationObject> parents = getOrgTagsSubList(orgTags, getTagsEdgesSubList(edges, entity.getId()));

                for (TagsRepresentationObject parent : parents){
                    for(TagsRepresentationObject tag : orgTags)
                        if (tag.getId().equals(parent.getId())) {
                            tag.children.add(entity);
                            break;
                        }
                }
                removedChildren.add(entity);
            }
        }
        orgTags.removeAll(removedChildren);
        return orgTags;
    }

    private List<Long> getTagsEdgesSubList(List<TagGraphEdgesEntity> e, Long id) {
        return e.stream().filter(tag -> tag.getChildId().equals(id)).map(tag -> tag.getParentId()).collect(Collectors.toList());
    }

    private List<TagsRepresentationObject> getOrgTagsSubList(List<TagsRepresentationObject> e, List<Long> ids) {
        return e.stream().filter(tag -> ids.contains(tag.getId())).collect(Collectors.toList());
    }




    public TagsEntity createOrgTag(TagsDTO tagDTO) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        TagsEntity entity = null;
        if(tagDTO.getOperation() == null)
            throw new BusinessException("MISSING PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        else if (tagDTO.getOperation().equals("create"))
            entity = verifyAndGetTagsEntityForCreate(tagDTO, org);

        else if(tagDTO.getOperation().equals("update")) {
            if (tagDTO.getId() == null)
                throw new BusinessException("MISSING PARAM: id", "id is required to update tag", HttpStatus.NOT_ACCEPTABLE);
            if (!orgTagsRepo.findById(tagDTO.getId()).isPresent())
                throw new BusinessException("INVALID PARAM: id", "No tag exists with provided id", HttpStatus.NOT_ACCEPTABLE);

            entity = orgTagsRepo.findById(tagDTO.getId()).get();
        } else {
            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + tagDTO.getOperation(), HttpStatus.NOT_ACCEPTABLE);
        }
        BeanUtils.copyProperties(tagDTO, entity, new String[]{"operation"});

        return orgTagsRepo.save(entity);
    }

    private TagsEntity verifyAndGetTagsEntityForCreate(TagsDTO tagDTO, OrganizationEntity org) throws BusinessException {
        TagsEntity entity = null;
        CategoriesEntity category = null;
        if (tagDTO.getCategoryId() == null)
            throw new BusinessException("MISSING PARAM: category_id", "category_id is required to create tag", HttpStatus.NOT_ACCEPTABLE);
        if (!categoryRepository.findById(tagDTO.getCategoryId()).isPresent())
            throw new BusinessException("INVALID PARAM: category_id", "No category exists with provided id", HttpStatus.NOT_ACCEPTABLE);
        if (orgTagsRepo.findByCategoriesEntity_IdAndOrganizationEntity_Id(tagDTO.getCategoryId(), org.getId()) != null)
            throw new BusinessException("INVALID PARAM: category_id, org_id", "The same category exists in the same organization", HttpStatus.NOT_ACCEPTABLE);

        category = categoryRepository.findById(tagDTO.getCategoryId()).get();
        entity = new TagsEntity();
        entity.setOrganizationEntity(org);
        entity.setCategoriesEntity(category);
        if (tagDTO.getAlias() == null)
            tagDTO.setAlias(categoryRepository.findById(tagDTO.getCategoryId()).get().getName());
        else
            entity.setPname(StringUtils.encodeUrl(tagDTO.getAlias()));
        return entity;
    }

    public void createTagEdges(TagsLinkDTO tagsLinks) throws BusinessException{
        if (tagsLinks.getParentId() == null)
            createTopLevelTagLinks(tagsLinks);
        else {
            validateTagLinkDTO(tagsLinks);

            Long parentId = tagsLinks.getParentId();
            List<Long> childrenIds = tagsLinks.getChildrenIds();

            OrganizationEntity org = securityService.getCurrentUserOrganization();
            List<TagsEntity> orgTags = orgTagsRepo.findByOrganizationEntity_Id(org.getId());

            Map<Long, TagsEntity> tagsMap = new HashMap<>();
            for (TagsEntity tag : orgTags) tagsMap.put(tag.getId(), tag);

            List<Pair> tagsEdges = tagEdgesRepo.getTagsLinks(tagsMap.keySet());

            DirectedAcyclicGraph<TagsEntity, DefaultEdge> tagsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
            for (TagsEntity i : orgTags) tagsGraph.addVertex(i);

            for (Pair i : tagsEdges)
                if (tagsMap.get(i.getFirst()) != null)
                    tagsGraph.addEdge(tagsMap.get(i.getSecond()), tagsMap.get(i.getFirst()));


            for (Long childId : childrenIds) {
                try {
                    if (tagsMap.get(childId) == null)
                        throw new BusinessException("INVALID PARAM: child_id",
                                "Provided child_id(" + childId + ") doesn't match any existing tag", HttpStatus.NOT_ACCEPTABLE);

                    if (tagsEdges.contains(new Pair(parentId, childId)))
                        throw new BusinessException("INVALID PARAM",
                                "Tag link with parent_id "+ parentId +" and child_id "+ childId+" already exist!", HttpStatus.NOT_ACCEPTABLE);

                    tagsGraph.addEdge(tagsMap.get(childId), tagsMap.get(parentId));
                    TagGraphEdgesEntity edge = new TagGraphEdgesEntity();
                    edge.setChildId(childId);
                    edge.setParentId(parentId);
                    tagEdgesRepo.save(edge);
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Cycle Deteted !",
                            "Creating a link between id " + childId + " and " + parentId + " would induce a cycle",
                            HttpStatus.NOT_ACCEPTABLE);
                }
            }
        }
    }

    public boolean deleteTagLink(TagsLinkDTO tagsLinks) throws BusinessException {
        if (tagsLinks.isUpdated("parentId") && tagsLinks.getParentId() == null)
            return deleteTopLevelTagLink(tagsLinks);
        else {
            validateTagLinkDTO(tagsLinks);

            Long orgId = securityService.getCurrentUserOrganizationId();
            Long parentId = tagsLinks.getParentId();
            List<Long> childrenIds = tagsLinks.getChildrenIds();
            TagGraphEdgesEntity edge;

            for (Long childId : childrenIds) {
                edge = tagEdgesRepo.findByParentIdAndChildId(parentId, childId);
                if (edge != null)
                    tagEdgesRepo.delete(edge);
                else
                    throw new BusinessException("INVALID PARAM", "Tag link with parent_id " + parentId + " and child_id " + childId + " doesn't exist!", HttpStatus.NOT_ACCEPTABLE);
            }
            return true;
        }
    }

    public boolean deleteTopLevelTagLink (TagsLinkDTO tagsLinks) throws BusinessException {
        if (tagsLinks.getChildrenIds() == null)
            throw new BusinessException("MISSING PARAM: children_ids", "Required children_ids are missing", HttpStatus.NOT_ACCEPTABLE);
        List<Long> childrenIds = tagsLinks.getChildrenIds();
        TagGraphEdgesEntity edge;
        for (Long childId : childrenIds) {
            edge = tagEdgesRepo.findByParentIdIsNullAndChildId(childId);
            if (edge == null)
                throw new BusinessException("INVALID PARAM: tag_id", "Top level tag link with id " + childId + " doesn't exist!", HttpStatus.NOT_ACCEPTABLE);

            tagEdgesRepo.delete(edge);
        }
        return true;
    }

    private boolean createTopLevelTagLinks(TagsLinkDTO tagsLinks) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();

        List<TagsEntity> orgTags = orgTagsRepo.findByIdInAndOrganizationEntity_Id(tagsLinks.getChildrenIds(), org.getId());

        Map<Long, TagsEntity> tagsMap = new HashMap<>();
        for(TagsEntity entity : orgTags) tagsMap.put(entity.getId(), entity);

        List<TagGraphEdgesEntity> tagEdges = tagEdgesRepo.findByParentIdNullAndChildIdIn(tagsMap.keySet());

        Map<Long, TagGraphEdgesEntity> tagEdgesMap = new HashMap<>();
        for(TagGraphEdgesEntity entity : tagEdges) tagEdgesMap.put(entity.getChildId(), entity);

        TagGraphEdgesEntity edge;
        for(Long childId : tagsLinks.getChildrenIds()){
            if (tagsMap.get(childId) == null)
                throw new BusinessException("INVALID PARAM: child_id", "provided child_id "+ childId +" doesn't match any existing tag!", HttpStatus.NOT_ACCEPTABLE);

            if (tagEdgesMap.get(childId) != null)
                throw new BusinessException("INVALID PARAM: child_id", "Provided Tag "+ childId +" has a parent and can't be a top level tag!", HttpStatus.NOT_ACCEPTABLE);

            edge = new TagGraphEdgesEntity();
            edge.setChildId(childId);
            tagEdgesRepo.save(edge);
        }
        return true;
    }


    private void validateTagLinkDTO(TagsLinkDTO tagsLinks) throws BusinessException {
        if (tagsLinks.getParentId() == null)
            throw new BusinessException("MISSING PARAM: parent_id", "Required parent_id is missing", HttpStatus.NOT_ACCEPTABLE);

        if (!orgTagsRepo.findById(tagsLinks.getParentId()).isPresent())
            throw new BusinessException("INVALID PARAM: parent_id", "Provided parent_id doesn't match any existing tag", HttpStatus.NOT_ACCEPTABLE);

        if (tagsLinks.getChildrenIds() == null)
            throw new BusinessException("MISSING PARAM: children_ids", "Required children_ids are missing", HttpStatus.NOT_ACCEPTABLE);
    }

}
