package com.nasnav.service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.ResponseStatus;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private OrganizationTagsRepository orgTagsRepo;

    @Autowired
    private TagsRepository tagsRepo;

    @Autowired
    private TagGraphEdgesRepository tagEdgesRepo;

    @Autowired
    private SecurityService securityService;

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

    public List<TagsEntity> getNasnavTags() {
        return tagsRepo.findAll();
    }

    public List<OrganizationTagsRepresentationObject> getOrganizationTags(Long orgId) {
        return orgTagsRepo.findByOrganizationEntity_Id(orgId)
                .stream()
                .map(tag ->(OrganizationTagsRepresentationObject) tag.getRepresentation())
                .collect(Collectors.toList());
    }

    public List<OrganizationTagsRepresentationObject> getOrganizationTagsTree(Long orgId) {

        List<TagGraphEdgesEntity> edges = tagEdgesRepo.findByOrganizationEntity_IdOrderByIdAsc(orgId);

        List<OrganizationTagsRepresentationObject> orgTags = orgTagsRepo.findByIdIn(
                    edges.stream().map(edge -> edge.getChildId()).collect(Collectors.toList()))
                .stream().map(tag ->(OrganizationTagsRepresentationObject) tag.getRepresentation())
                .collect(Collectors.toList());

        List <OrganizationTagsRepresentationObject> removedChildren = new ArrayList<>();

        for(OrganizationTagsRepresentationObject entity : orgTags){
            if(tagEdgesRepo.findByParentIdNotNullAndChildIdAndOrganizationEntity_Id(entity.getId(), orgId) != null) {

                List<OrganizationTagsRepresentationObject> parents = getOrgTagsSubList(orgTags, getTagsEdgesSubList(edges, entity.getId()));

                for (OrganizationTagsRepresentationObject parent : parents){
                    for(OrganizationTagsRepresentationObject tag : orgTags)
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

    private List<OrganizationTagsRepresentationObject> getOrgTagsSubList(List<OrganizationTagsRepresentationObject> e, List<Long> ids) {
        return e.stream().filter(tag -> ids.contains(tag.getId())).collect(Collectors.toList());
    }

    public TagsEntity createTag(TagsDTO tagDTO) throws BusinessException {
        TagsEntity tag = null;
        if(tagDTO.getOperation() == null)
            throw new BusinessException("MISSING PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        else if (tagDTO.getOperation().equals("create")) {
            if (tagDTO.getName() == null)
                throw new BusinessException("MISSING PARAM: name", "Required name mustn't be null", HttpStatus.NOT_ACCEPTABLE);
            if (tagsRepo.findByNameIgnoreCase(tagDTO.getName()) != null)
                throw new BusinessException("INVALID PARAM: name", "Another tag exists with the same name", HttpStatus.NOT_ACCEPTABLE);
            tag = new TagsEntity();
            tag.setName(tagDTO.getName());
        } else if(tagDTO.getOperation().equals("update")) {
            if (tagDTO.getId() == null)
                throw new BusinessException("MISSING PARAM: id", "Required id mustn't be null", HttpStatus.NOT_ACCEPTABLE);
            if (!tagsRepo.findById(tagDTO.getId()).isPresent())
                throw new BusinessException("INVALID PARAM: id", "No tag exists with provided id", HttpStatus.NOT_ACCEPTABLE);
            if (tagsRepo.findByNameIgnoreCase(tagDTO.getName()) != null)
                throw new BusinessException("INVALID PARAM: name", "Another tag exists with the same name", HttpStatus.NOT_ACCEPTABLE);
            tag = tagsRepo.findById(tagDTO.getId()).get();
            tag.setName(tagDTO.getName());
        } else {
            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + tagDTO.getOperation(), HttpStatus.NOT_ACCEPTABLE);
        }
        return tagsRepo.save(tag);
    }

    public OrganizationTagsEntity createOrgTag(OrganizationTagsDTO tagDTO) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        TagsEntity tag = null;
        OrganizationTagsEntity entity = null;
        if(tagDTO.getOperation() == null)
            throw new BusinessException("MISSING PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        else if (tagDTO.getOperation().equals("create")) {
            if (tagDTO.getTagId() == null)
                throw new BusinessException("MISSING PARAM: tag_id", "Nasnav tag_id is required to create tag", HttpStatus.NOT_ACCEPTABLE);
            if (!tagsRepo.findById(tagDTO.getTagId()).isPresent())
                throw new BusinessException("INVALID PARAM: tag_id", "No tag exists with provided id", HttpStatus.NOT_ACCEPTABLE);
            if (orgTagsRepo.findByTagsEntity_IdAndOrganizationEntity_Id(tagDTO.getTagId(), org.getId()) != null)
                throw new BusinessException("INVALID PARAM: tag_id, org_id", "The same tag exists in the same organization", HttpStatus.NOT_ACCEPTABLE);

            tag = tagsRepo.findById(tagDTO.getTagId()).get();
            entity = new OrganizationTagsEntity();
            entity.setOrganizationEntity(org);
            entity.setTagsEntity(tag);
        } else if(tagDTO.getOperation().equals("update")) {
            if (tagDTO.getId() == null)
                throw new BusinessException("MISSING PARAM: id", "id is required to update tag", HttpStatus.NOT_ACCEPTABLE);
            if (!orgTagsRepo.findById(tagDTO.getId()).isPresent())
                throw new BusinessException("INVALID PARAM: id", "No tag exists with provided id", HttpStatus.NOT_ACCEPTABLE);

            entity = orgTagsRepo.findById(tagDTO.getId()).get();
        } else {
            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + tagDTO.getOperation(), HttpStatus.NOT_ACCEPTABLE);
        }
        BeanUtils.copyProperties(tagDTO, entity, new String[]{"operation"});
        if (tagDTO.getAlias() == null) tagDTO.setAlias(tagsRepo.findById(tagDTO.getTagId()).get().getName());
        if (tagDTO.getAlias() != null)
            entity.setPname(StringUtils.encodeUrl(tagDTO.getAlias()));

        return orgTagsRepo.save(entity);
    }

    public void createTagEdges(TagsLinkDTO tagsLinks) throws BusinessException{
        if (tagsLinks.getParentId() == null)
            createTopLevelTagLinks(tagsLinks);
        else {
            validateTagLinkDTO(tagsLinks);

            Long parentId = tagsLinks.getParentId();
            List<Long> childrenIds = tagsLinks.getChildrenIds();

            OrganizationEntity org = securityService.getCurrentUserOrganization();
            List<OrganizationTagsEntity> orgTags = orgTagsRepo.findByOrganizationEntity_Id(org.getId());
            List<TagGraphEdgesEntity> tagsEdges = tagEdgesRepo.findByOrganizationEntity_IdOrderByIdAsc(org.getId());

            Map<Long, OrganizationTagsEntity> tagsMap = new HashMap<>();
            for (OrganizationTagsEntity tag : orgTags) tagsMap.put(tag.getId(), tag);

            DirectedAcyclicGraph<OrganizationTagsEntity, DefaultEdge> tagsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
            for (OrganizationTagsEntity i : orgTags) tagsGraph.addVertex(i);

            for (TagGraphEdgesEntity i : tagsEdges)
                if (tagsMap.get(i.getParentId()) != null)
                    tagsGraph.addEdge(tagsMap.get(i.getChildId()), tagsMap.get(i.getParentId()));


            for (Long childId : childrenIds) {
                try {
                    if (tagsMap.get(childId) == null)
                        throw new BusinessException("INVALID PARAM: child_id",
                                "Provided child_id(" + childId + ") doesn't match any existing tag", HttpStatus.NOT_ACCEPTABLE);

                    if (tagEdgesRepo.findByParentIdAndChildIdAndOrganizationEntity_Id(parentId, childId, org.getId()) != null)
                        throw new BusinessException("INVALID PARAM",
                                "Tag link with parent_id "+ parentId +" and child_id "+ childId+" already exist!", HttpStatus.NOT_ACCEPTABLE);

                    tagsGraph.addEdge(tagsMap.get(childId), tagsMap.get(parentId));
                    TagGraphEdgesEntity edge = new TagGraphEdgesEntity();
                    edge.setChildId(childId);
                    edge.setParentId(parentId);
                    edge.setOrganizationEntity(org);
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
        validateTagLinkDTO(tagsLinks);

        Long orgId = securityService.getCurrentUserOrganizationId();
        Long parentId = tagsLinks.getParentId();
        List<Long> childrenIds = tagsLinks.getChildrenIds();
        TagGraphEdgesEntity edge;

        for(Long childId : childrenIds){
            edge = tagEdgesRepo.findByParentIdAndChildIdAndOrganizationEntity_Id(parentId, childId, orgId);
            if (edge != null)
                tagEdgesRepo.delete(edge);
            else
                throw new BusinessException("INVALID PARAM", "Tag link with parent_id "+ parentId +" and child_id "+ childId+" doesn't exist!", HttpStatus.NOT_ACCEPTABLE);
        }
        return true;
    }

    private boolean createTopLevelTagLinks(TagsLinkDTO tagsLinks) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        TagGraphEdgesEntity edge;
        for(Long childId : tagsLinks.getChildrenIds()){
            if (orgTagsRepo.findByIdAndOrganizationEntity_Id(childId, org.getId()) == null)
                throw new BusinessException("INVALID PARAM: child_id", "provided child_id "+ childId +" doesn't match any existing tag!", HttpStatus.NOT_ACCEPTABLE);

            if (tagEdgesRepo.findByParentIdNotNullAndChildIdAndOrganizationEntity_Id(childId, org.getId()) != null)
                throw new BusinessException("", "Provided Tag "+ childId +" has a parent and can't be a top level tag!", HttpStatus.NOT_ACCEPTABLE);

            edge = new TagGraphEdgesEntity();
            edge.setChildId(childId);
            edge.setOrganizationEntity(org);
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
