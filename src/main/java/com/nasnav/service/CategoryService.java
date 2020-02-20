package com.nasnav.service;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nasnav.commons.utils.EntityUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dao.TagGraphEdgesRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.CategoryDTO;
import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.Pair;
import com.nasnav.dto.TagsDTO;
import com.nasnav.dto.TagsLinkDTO;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.TagGraphEdgesEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.TagResponse;

@Service
public class CategoryService {

    @Autowired
    private final BrandsRepository brandsRepository;

    @Autowired
    private  CategoriesRepository categoryRepository;

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
    public CategoryService(BrandsRepository brandsRepository, ProductRepository productRepository) {
        this.brandsRepository = brandsRepository;
        this.productRepository = productRepository;
    }

    
    
    

    public List<CategoryRepresentationObject> getCategories(Long organizationId, Long categoryId){
    	List<CategoriesEntity> categoriesEntityList = new ArrayList<>();
    	if(organizationId == null && categoryId == null) {
    		categoriesEntityList =  categoryRepository.findAll();
    	}else if(categoryId == null) {
    		categoriesEntityList = categoryRepository.findByOrganizationId(organizationId);  
    	}else if(organizationId == null && categoryId != null) {
        	categoriesEntityList = 
        			categoryRepository.findById(categoryId)
        					.map(Arrays::asList)
        					.map(ArrayList::new)
        					.orElse(new ArrayList<>());        	
        	if(!categoriesEntityList.isEmpty()) {
        		List<CategoriesEntity> children = categoryRepository.findByParentId(categoryId.intValue());
            	children.forEach(categoriesEntityList::add);
        	}     	        					
    	}else {
    		categoriesEntityList = categoryRepository.findByOrganizationIdAndCategoryId(organizationId, categoryId);
    	}    	
    	
        return categoriesEntityList
        			.stream()
					.map(category -> (CategoryRepresentationObject) category.getRepresentation())
					.sorted(comparing(CategoryRepresentationObject::getName))
					.collect(toList());
    }
    
    
    

    public ResponseEntity<CategoryResponse> createCategory(CategoryDTO.CategoryModificationObject categoryJson) throws BusinessException {
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
        return new ResponseEntity<CategoryResponse>(new CategoryResponse(categoriesEntity.getId()), HttpStatus.OK);
    }

    public ResponseEntity<CategoryResponse> updateCategory(CategoryDTO.CategoryModificationObject categoryJson) throws BusinessException {
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
        return new ResponseEntity<CategoryResponse>(new CategoryResponse(categoriesEntity.getId()), HttpStatus.OK);
    }

    public ResponseEntity<CategoryResponse> deleteCategory(Long categoryId) throws BusinessException {
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
        return new ResponseEntity<CategoryResponse>(new CategoryResponse(categoriesEntity.getId()),HttpStatus.OK);
    }






    public List<TagsRepresentationObject> getOrganizationTags(Long orgId, String categoryName) {
        List<TagsEntity> tagsEntities;
        if(isBlankOrNull(categoryName)) {
        	tagsEntities = orgTagsRepo.findByOrganizationEntity_Id(orgId);
        }else {
        	tagsEntities = orgTagsRepo.findByCategoriesEntity_NameAndOrganizationEntity_Id(categoryName, orgId);
        }
        return tagsEntities
        			.stream()
                    	.map(tag ->(TagsRepresentationObject) tag.getRepresentation())
                    	.collect(toList());
    }

    public List<TagsRepresentationObject> getOrganizationTagsTree(Long orgId) throws BusinessException {

        OrganizationEntity org = orgRepo.getOne(orgId);
        if (org == null)
            throw new BusinessException("Provided org_id doesn't match any existing organization",
                                        "INVALID_PARAM: org_id", HttpStatus.NOT_ACCEPTABLE);

        List<TagsRepresentationObject> orgTags = orgTagsRepo.getTagsByOrgId(org)
                .stream().map(tag ->(TagsRepresentationObject) tag.getRepresentation())
                .collect(Collectors.toList());

        if (orgTags == null || orgTags.isEmpty())
            return new ArrayList<>();

        List<TagGraphEdgesEntity> edges = tagEdgesRepo.getTagsLinks(orgTags.stream().map(tag -> tag.getId()).collect(Collectors.toList()));

        List <TagsRepresentationObject> removedChildren = new ArrayList<>();

        List<TagsRepresentationObject> parents;

        for(TagsRepresentationObject entity : orgTags){

            parents = getOrgTagsSubList(orgTags, getTagsEdgesSubList(edges, entity.getId()));

            if(!parents.isEmpty()) {

                for (TagsRepresentationObject parent : parents){
                    for(TagsRepresentationObject tag : orgTags) {
                        if (tag.getId().equals(parent.getId())) {
                            tag.children.add(entity);
                            break;
                        }
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




    public TagsEntity createOrgTag(TagsDTO tagDTO) throws BusinessException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        TagsEntity entity = null;
        if(tagDTO.getOperation() == null)
            throw new BusinessException("MISSING PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        else if (tagDTO.getOperation().equals("create"))
            entity = verifyAndGetTagsEntityForCreate(tagDTO, org);

        else if(tagDTO.getOperation().equals("update")) {
            if (tagDTO.getId() == null)
                throw new BusinessException("MISSING PARAM: id", "id is required to update tag", HttpStatus.NOT_ACCEPTABLE);

            entity = orgTagsRepo.findByIdAndOrganizationEntity_Id(tagDTO.getId(), org.getId());
            if (entity == null)
                throw new BusinessException("INVALID PARAM: id", "No tag exists in the organization with provided id", HttpStatus.NOT_ACCEPTABLE);
        } else {
            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + tagDTO.getOperation(), HttpStatus.NOT_ACCEPTABLE);
        }

        EntityUtils.copyNonNullProperties(tagDTO, entity);

        if (tagDTO.isUpdated("graphId")) {
            if(tagDTO.getGraphId() == null) {
                if (tagEdgesRepo.getTagsLinks(Collections.singletonList(tagDTO.getId())).size() > 0)
                    throw new BusinessException("Can't set graph_id to null while the tag is linked to other tags !",
                            "INVALID_PARAM: graph_id", HttpStatus.NOT_ACCEPTABLE);
                entity.setGraphId(null);
            }
            else
                entity.setGraphId(org.getId().intValue()); // TODO will change to tagDTO.getGraphId() when we support MultiGraph per org
        }

        return orgTagsRepo.save(entity);
    }

    private TagsEntity verifyAndGetTagsEntityForCreate(TagsDTO tagDTO, OrganizationEntity org) throws BusinessException {
        TagsEntity entity = null;
        CategoriesEntity category = null;
        if (tagDTO.getCategoryId() == null)
            throw new BusinessException("MISSING PARAM: category_id", "category_id is required to create tag", HttpStatus.NOT_ACCEPTABLE);
        if (!categoryRepository.findById(tagDTO.getCategoryId()).isPresent())
            throw new BusinessException("INVALID PARAM: category_id", "No category exists with provided id", HttpStatus.NOT_ACCEPTABLE);

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

    public void createTagEdges(List<TagsLinkDTO> tagsLinksDTOs) throws BusinessException{

        OrganizationEntity org = securityService.getCurrentUserOrganization();
        for(TagsLinkDTO tagsLinks : tagsLinksDTOs) {
            validateTagLinkDTO(tagsLinks, org.getId()); // validating each tagsLinksDTO has existing parent and a list of children
        }

        List<TagsEntity> orgTags = orgTagsRepo.findByOrganizationEntity_Id(org.getId());

        Map<Long, TagsEntity> tagsMap = new HashMap<>();
        for (TagsEntity tag : orgTags) tagsMap.put(tag.getId(), tag); // creating tags map to easier search for specific tag

        List<Pair> tagsEdges = tagEdgesRepo.getTagsLinks(tagsMap.keySet()); //getting tags links already existing in the system

        DirectedAcyclicGraph<TagsEntity, DefaultEdge> tagsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class); // building DAG
        for (TagsEntity i : orgTags) //adding vertices
            tagsGraph.addVertex(i);

        for (Pair i : tagsEdges) //adding edges
            if (tagsMap.get(i.getFirst()) != null)
                tagsGraph.addEdge(tagsMap.get(i.getSecond()), tagsMap.get(i.getFirst()));

        for(TagsLinkDTO tagsLinks : tagsLinksDTOs) {
            Long parentId = tagsLinks.getParentId();
            if (isBlankOrNull(tagsLinks.getChildrenIds())) { // for creating top level tags only
                setTagGraphId(tagsMap, parentId, org.getId());
                continue;
            } else {
                List<Long> childrenIds = tagsLinks.getChildrenIds();
                for (Long childId : childrenIds) { // trying to add new links(edges) to graph
                    try {
                        if (tagsMap.get(childId) == null)
                            throw new BusinessException("INVALID_PARAM: child_id",
                                    "Provided child_id(" + childId + ") doesn't match any existing tag", HttpStatus.NOT_ACCEPTABLE);

                        if (tagsEdges.contains(new Pair(parentId, childId)))
                            throw new BusinessException("INVALID_PARAM",
                                    "Tag link with parent_id " + parentId + " and child_id " + childId + " already exist!", HttpStatus.NOT_ACCEPTABLE);

                        tagsGraph.addEdge(tagsMap.get(childId), tagsMap.get(parentId));

                    } catch (IllegalArgumentException e) {
                        throw new BusinessException("Cycle Detected !",
                                "Creating a link between id " + childId + " and " + parentId + " would induce a cycle",
                                HttpStatus.NOT_ACCEPTABLE);
                    }
                }
            }
        }
        for(TagsLinkDTO tagsLinks : tagsLinksDTOs) {
            if (isBlankOrNull(tagsLinks.getChildrenIds()))
                continue;
            Long parentId = tagsLinks.getParentId();
            List<Long> childrenIds = tagsLinks.getChildrenIds();
            for(Long childId: childrenIds){
                addTagsLink(parentId, childId);
                setTagGraphId(tagsMap, parentId, org.getId());
                setTagGraphId(tagsMap, childId, org.getId());
            }
        }
    }

    private void addTagsLink(Long parentId, Long childId) {
        TagGraphEdgesEntity edge = new TagGraphEdgesEntity();
        edge.setChildId(childId);
        edge.setParentId(parentId);
        tagEdgesRepo.save(edge);
    }

    private void setTagGraphId(Map<Long, TagsEntity> tagsMap, Long tagId, Long orgId) {
        TagsEntity entity = tagsMap.get(tagId);
        entity.setGraphId(orgId.intValue());
        orgTagsRepo.save(tagsMap.get(tagId));
    }

    public boolean deleteTagLink(List<TagsLinkDTO> tagsLinksDTOs) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();

        for(TagsLinkDTO tagsLinks : tagsLinksDTOs) {
            validateTagLinkDTO(tagsLinks, orgId);
            validateChildrenIds(tagsLinks);

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
        }
        return true;
    }


    private void validateTagLinkDTO(TagsLinkDTO tagsLinks, Long orgId) throws BusinessException {
        if (tagsLinks.getParentId() == null)
            throw new BusinessException("MISSING PARAM: parent_id", "Required parent_id is missing", HttpStatus.NOT_ACCEPTABLE);

        if (orgTagsRepo.findByIdAndOrganizationEntity_Id(tagsLinks.getParentId(), orgId) == null)
            throw new BusinessException("INVALID PARAM: parent_id", "Provided parent_id doesn't match any existing tag", HttpStatus.NOT_ACCEPTABLE);
    }

    private void validateChildrenIds(TagsLinkDTO tagsLinks) throws BusinessException {
        if (tagsLinks.getChildrenIds() == null)
            throw new BusinessException("MISSING PARAM: children_ids", "Required children_ids are missing", HttpStatus.NOT_ACCEPTABLE);
    }

    public TagResponse deleteOrgTag(Long tagId) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();
        TagsEntity tag = orgTagsRepo.findByIdAndOrganizationEntity_Id(tagId, orgId);

        if (tag == null)
            throw new BusinessException("Provided tag_id doesn't match any existing tag","INVALID_PARAM: tag_id", HttpStatus.NOT_ACCEPTABLE);

        List<TagGraphEdgesEntity> tags = tagEdgesRepo.getTagsLinks(Collections.singletonList(tagId));
        if (!tags.isEmpty())
            throw new BusinessException("There are tags Linked to this tag!","NOT_EMPTY:tags Links",HttpStatus.NOT_ACCEPTABLE);

        List<Long> products = productRepository.getProductIdsByTagsList(Collections.singletonList(tagId));
        if (!products.isEmpty())
            throw new BusinessException("There are products "+ products +" connected to this tag!","NOT_EMPTY:products",HttpStatus.NOT_ACCEPTABLE);

        orgTagsRepo.delete(tag);
        return new TagResponse(tag.getId());
    }

}
