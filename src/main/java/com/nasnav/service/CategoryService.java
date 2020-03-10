package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.copyNonNullProperties;
import static com.nasnav.commons.utils.StringUtils.encodeUrl;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.TagGraphEdgesRepository;
import com.nasnav.dao.TagGraphNodeRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.CategoryDTO;
import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.TagsDTO;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.TagsTreeCreationDTO;
import com.nasnav.dto.TagsTreeNodeDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.TagGraphEdgesEntity;
import com.nasnav.persistence.TagGraphNodeEntity;
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
    private TagGraphNodeRepository tagNodesRepo;

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

    
    
    
    private List<Long> getTagsEdgesSubList(List<TagGraphEdgesEntity> edges, Long id) {
        return edges
        		.stream()
        		.filter(tag -> 
        				ofNullable(tag)
        				.map(TagGraphEdgesEntity::getChild)
        				.map(TagGraphNodeEntity::getId)
        				.orElse(-1L)
        				.equals(id))
        		.map(TagGraphEdgesEntity::getParent)
        		.map(TagGraphNodeEntity::getId)
        		.collect(toList());
    }

    
    
    
    private List<TagsRepresentationObject> getOrgTagsSubList(List<TagsRepresentationObject> edges, List<Long> ids) {
        return edges.stream()
        		.filter(tag -> ids.contains(tag.getId()))
        		.collect(toList());
    }



    

    public TagsEntity createOrUpdateTag(TagsDTO tagDTO) throws BusinessException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        validateTagDto(tagDTO);
        
        String operation = tagDTO.getOperation();
        
        TagsEntity entity = null;
        if(Objects.equals(operation, "create")) {
        	entity = createNewTag(tagDTO);
        }else {
        	entity = updateTag(tagDTO);
        }    

        return orgTagsRepo.save(entity);
    }





	private TagsEntity updateTag(TagsDTO tagDTO)
			throws BusinessException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		TagsEntity entity;
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		entity = orgTagsRepo
					.findByIdAndOrganizationEntity_Id(tagDTO.getId(), org.getId())
					.orElseThrow(() -> new BusinessException(
											"INVALID PARAM: id"
											, "No tag exists in the organization with provided id"
											, NOT_ACCEPTABLE));
		
		copyNonNullProperties(tagDTO, entity);

		if (tagDTO.isUpdated("graphId")) {
			Integer graphId = tagDTO.getGraphId() != null? org.getId().intValue() : null;
			entity.setGraphId(graphId);            	
		}
		return entity;
	}





	private void validateTagDto(TagsDTO tagDTO) throws BusinessException {
		String operation = tagDTO.getOperation();
        Long categoryId = tagDTO.getCategoryId();
        
        if( isBlankOrNull(operation)) {
        	throw new BusinessException("MISSING PARAM: operation", "", NOT_ACCEPTABLE);
        }else if(Objects.equals(operation, "create")) {
        	if (categoryId == null) {
            	throw new BusinessException("MISSING PARAM: category_id", "category_id is required to create tag", NOT_ACCEPTABLE);
            }else if (tagDTO.getName() == null) {
            	throw new BusinessException("MISSING PARAM: name", "name is required to create tag", NOT_ACCEPTABLE);
            }
            
        }else if(Objects.equals(operation, "update")) {
			 if (tagDTO.getId() == null) {
				 throw new BusinessException("MISSING PARAM: id", "id is required to update tag", NOT_ACCEPTABLE);
			 }  
			 if (isNullGraphIdForTagWithRelations(tagDTO)) {
			         throw new BusinessException(
			        		 		"Can't set graph_id to null while the tag is linked to other tags !"
			        		 		, "INVALID_PARAM: graph_id"
			        		 		, NOT_ACCEPTABLE);
			 }
        }else {
            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + tagDTO.getOperation(), NOT_ACCEPTABLE);
        }
	}





	private boolean isNullGraphIdForTagWithRelations(TagsDTO tagDTO) {
		Integer tagLinksCount = 
				tagEdgesRepo
				 .getTagsLinks(singletonList(tagDTO.getId()))
				 .size();
		return tagDTO.isUpdated("graphId") 
				 && tagDTO.getGraphId() == null 
				 &&  tagLinksCount > 0;
	}
        
        
        
        

    private TagsEntity createNewTag(TagsDTO tagDTO) throws BusinessException {
    	OrganizationEntity org = securityService.getCurrentUserOrganization();
    	CategoriesEntity category = 
        		categoryRepository
        			.findById(tagDTO.getCategoryId())
        			.orElseThrow(() -> new BusinessException("INVALID PARAM: category_id", "No category exists with provided id", NOT_ACCEPTABLE));

    	String alias = ofNullable(tagDTO.getAlias()).orElse(tagDTO.getName());
       
    	TagsEntity entity = new TagsEntity();
        entity.setOrganizationEntity(org);
        entity.setCategoriesEntity(category);
        entity.setName(tagDTO.getName());
        entity.setAlias(alias);
        entity.setPname(encodeUrl(tagDTO.getName()));
        entity.setGraphId(tagDTO.getGraphId());
        entity.setMetadata(tagDTO.getMetadata());
        if(tagDTO.getGraphId() != null) {
        	entity.setGraphId(org.getId().intValue()); // TODO will change to tagDTO.getGraphId() when we support MultiGraph per org
        }
        
        return entity;
    }
    
    

    
    @Transactional(rollbackFor = Throwable.class)
    public void createTagTree(TagsTreeCreationDTO tagsTreeDTO) throws BusinessException{

        List<TagsTreeNodeDTO> tree = tagsTreeDTO.getTreeNodes();

        if(tree == null || tree.isEmpty()) {
        	return;
        }

        
        Map<Long, TagsEntity> tagsMap = createOrgTagsMap();        
        
        clearTagsTree();
        buildNewTagGraph(tree, tagsMap);        
        persistTagGraph(tree, tagsMap);
    }





	private void persistTagGraph(List<TagsTreeNodeDTO> tagsLinksDTOs, Map<Long, TagsEntity> tagsMap) {
		for(TagsTreeNodeDTO tagsLinks : tagsLinksDTOs) {
        	Long parentId = tagsLinks.getParentId();
        	setTagGraphId(tagsMap, parentId);
        	
        	for (Long childId : getTagChildren(tagsLinks)) {
	        	 persistTagsLink(parentId, childId, tagsMap);	             
	             setTagGraphId(tagsMap, childId);
        	}
        }
	}





	private void buildNewTagGraph(List<TagsTreeNodeDTO> tree, Map<Long, TagsEntity> tagsMap)
			throws BusinessException {
		tree.forEach(node -> createTagSubTree(node, tagsMap));
	}



	
	
	private TagGraphNodeEntity createTagSubTree(TagsTreeNodeDTO node, Map<Long, TagsEntity> tagsMap) {
		List<TagGraphNodeEntity> childrenNodes = createSubTreesForChildrenNodes(node, tagsMap);		
		TagGraphNodeEntity	savedNode = persistTagTreeNode(node, tagsMap);
		
		childrenNodes
		 .stream()
		 .map(child -> new TagGraphEdgesEntity(savedNode, child))
		 .forEach(tagEdgesRepo::save);
		
		return savedNode;
	}





	private TagGraphNodeEntity persistTagTreeNode(TagsTreeNodeDTO node, Map<Long, TagsEntity> tagsMap) {
		return ofNullable(node)
				.map(TagsTreeNodeDTO::getTagId)
				.map(tagsMap::get)
				.map(TagGraphNodeEntity::new)
				.map(tagNodesRepo::save)
				.orElseThrow(() -> 
					new RuntimeBusinessException(
							format("Failed to create Tree node for tag with id[%d]!", node.getTagId())
							, "INVALID PARAM: nodes"
							, NOT_ACCEPTABLE));
	}





	private List<TagGraphNodeEntity> createSubTreesForChildrenNodes(TagsTreeNodeDTO node,
			Map<Long, TagsEntity> tagsMap) {
		return ofNullable(node)
				.map(TagsTreeNodeDTO::getChildren)
				.orElse(emptyList())
				.stream()
				.map(child -> createTagSubTree(child, tagsMap))
				.collect(toList());
	}
	
	
	
	private  TagGraphNodeEntity toTagGraphNodeEntity(TagsEntity tag) {
		return new TagGraphNodeEntity(tag);
	}
	
	


	private List<Long> getTagChildren(TagsTreeNodeDTO tagsLinks) {
		return ofNullable(tagsLinks.getChildrenIds())
				.orElse(emptyList());
	}





	private void validateTagEdge(Map<Long, TagsEntity> tagsMap, List<TagGraphEdgesEntity> tagsEdges, Long parentId, Long childId)
			throws BusinessException {
		if (tagsMap.get(childId) == null)
		    throw new BusinessException("INVALID_PARAM: child_id",
		            "Provided child_id(" + childId + ") doesn't match any existing tag", NOT_ACCEPTABLE);
	}
	
	




	private DirectedAcyclicGraph<TagGraphNodeEntity, DefaultEdge> createCurrentStateTagsGraph(Map<Long, TagsEntity> tagsMap,
			List<TagGraphEdgesEntity> tagsEdges, List<TagGraphNodeEntity> currentTagNodes) {
        DirectedAcyclicGraph<TagGraphNodeEntity, DefaultEdge> tagsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);        
        currentTagNodes.forEach(tagsGraph::addVertex);
        tagsEdges.forEach(edge -> tagsGraph.addEdge(edge.getChild(), edge.getParent()));            
		return tagsGraph;
	}





	private Map<Long, TagsEntity> createOrgTagsMap() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		List<TagsEntity> orgTagsss = orgTagsRepo.findByOrganizationEntity_Id(orgId);

        Map<Long, TagsEntity> tagsMap = new HashMap<>();
        for (TagsEntity tag : orgTagsss) 
        	tagsMap.put(tag.getId(), tag); // creating tags map to easier search for specific tag
		return tagsMap;
	}

	
	
	
	
    private void persistTagsLink(Long parentId, Long childId, Map<Long, TagsEntity> tagsMap) {        
        TagsEntity parentTag = tagsMap.get(parentId);
        TagsEntity childTag = tagsMap.get(childId);
        
        TagGraphNodeEntity parentNode = new TagGraphNodeEntity(parentTag);
        TagGraphNodeEntity childeNode = new TagGraphNodeEntity(childTag);
        
        TagGraphEdgesEntity edge = new TagGraphEdgesEntity(parentNode, childeNode);
        tagEdgesRepo.save(edge);
    }

    
    
    private void setTagGraphId(Map<Long, TagsEntity> tagsMap, Long tagId) {
    	Long orgId = securityService.getCurrentUserOrganizationId();
        TagsEntity entity = tagsMap.get(tagId);
        entity.setGraphId(orgId.intValue());
        orgTagsRepo.save(tagsMap.get(tagId));
    }

    
    
    
    
    public boolean deleteTagLink(List<TagsTreeNodeDTO> tagsLinksDTOs) throws BusinessException {
        for(TagsTreeNodeDTO tagsLinks : tagsLinksDTOs) {
            validateTagLinkDTO(tagsLinks);
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


    
    
    
    
    private void validateTagLinkDTO(TagsTreeNodeDTO tagTreeNode){
    	Long orgId = securityService.getCurrentUserOrganizationId();
    	
        if (tagTreeNode.getParentId() == null)
            throw new RuntimeBusinessException("MISSING PARAM: parent_id", "Required parent_id is missing", NOT_ACCEPTABLE);

        if (!orgTagsRepo.findByIdAndOrganizationEntity_Id(tagTreeNode.getParentId(), orgId).isPresent())
            throw new RuntimeBusinessException("INVALID PARAM: parent_id", "Provided parent_id doesn't match any existing tag", NOT_ACCEPTABLE);
    }

    
    
    
    private void validateChildrenIds(TagsTreeNodeDTO tagsLinks) throws BusinessException {
        if (tagsLinks.getChildrenIds() == null)
            throw new BusinessException("MISSING PARAM: children_ids", "Required children_ids are missing", NOT_ACCEPTABLE);
    }

    
    
    
    public TagResponse deleteOrgTag(Long tagId) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();
        TagsEntity tag = 
        		orgTagsRepo
					.findByIdAndOrganizationEntity_Id(tagId, orgId)
					.orElseThrow(() -> new BusinessException("Provided tag_id doesn't match any existing tag","INVALID_PARAM: tag_id", HttpStatus.NOT_ACCEPTABLE));

        List<TagGraphEdgesEntity> tags = tagEdgesRepo.getTagsLinks(Collections.singletonList(tagId));
        if (!tags.isEmpty())
            throw new BusinessException("There are tags Linked to this tag!","NOT_EMPTY:tags Links",HttpStatus.NOT_ACCEPTABLE);

        productRepository.detachProductsFromTag(tagId);

        orgTagsRepo.delete(tag);

        return new TagResponse(tag.getId());
    }

    
    
    
    
    private void clearTagsTree() {
    	Long orgId = securityService.getCurrentUserOrganizationId();
        List<TagGraphEdgesEntity> tagsEdges = tagEdgesRepo.findByOrganizationId(orgId);
        tagEdgesRepo.deleteAll(tagsEdges);
        List<TagsEntity> tags = orgTagsRepo.findByOrganizationEntity_Id(orgId);
        for(TagsEntity tag : tags) {
            tag.setGraphId(null);
            orgTagsRepo.save(tag);
        }
    }

}


