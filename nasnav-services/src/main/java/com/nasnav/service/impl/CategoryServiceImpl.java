package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomOffsetAndLimitPageRequest;
import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.response.CategoryDto;
import com.nasnav.enumerations.AddonType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.TagResponse;
import com.nasnav.service.CategoryService;
import com.nasnav.service.SecurityService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nasnav.cache.Caches.*;
import static com.nasnav.commons.utils.EntityUtils.copyNonNullProperties;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.encodeUrl;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.isEmpty;

@Service
public class CategoryServiceImpl implements CategoryService {

	Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private BrandsRepository brandsRepository;
    @Autowired
    private CategoriesRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TagsRepository orgTagsRepo;
    @Autowired
    private TagGraphEdgesRepository tagEdgesRepo;
    @Autowired
    private TagGraphNodeRepository tagNodesRepo;
	@Autowired
	private OrganizationRepository orgRepo;
	

    @Autowired
    private SecurityService securityService;

	@Override
		public TagsRepresentationObject getTagById(Long tagId) throws BusinessException {
		return ofNullable(orgTagsRepo.findById(tagId))
				.get()
				.map(tag -> (TagsRepresentationObject) tag.getRepresentation())
				.orElseThrow(() -> new BusinessException("No tag found with given id!", "INVALID_PARAM: tag_id", HttpStatus.NOT_FOUND));
	}
    
//    @CacheResult(cacheName = "organizations_categories")
    @Override
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
        		List<CategoriesEntity> children = categoryRepository.findByParentId(categoryId);
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
    
    @Override
		public CategoryResponse addOrUpdateCategory(CategoryDTO categoryDTO) {
		String operation = ofNullable(categoryDTO.getOperation())
				.filter(o -> o.equals("create") || o.equals("update"))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0007));
		CategoriesEntity category = new CategoriesEntity();
		if (operation.equals("update"))
			category = updateCategory(categoryDTO);
		else if (operation.equals("create"))
			category = createCategory(categoryDTO);
		return new CategoryResponse(category.getId());
	}
    

    @Override
		public CategoriesEntity createCategory(CategoryDTO categoryJson) {
        CategoriesEntity categoriesEntity = new CategoriesEntity();

		validateCategoryName(categoryJson);
        categoriesEntity.setName(categoryJson.getName());
		categoriesEntity.setPname(StringUtils.encodeUrl(categoryJson.getName()));

		setCategoryEntityAdditionalInfo(categoriesEntity, categoryJson);

		return categoryRepository.save(categoriesEntity);
    }

    private void setCategoryEntityAdditionalInfo(CategoriesEntity entity, CategoryDTO dto) {
		if (dto.getParentId() != null) {
			validateCategoryParent(dto);
			entity.setParentId(dto.getParentId());
		}
    	if(dto.getLogo() != null) {
    		entity.setLogo(dto.getLogo());
		}
		if(dto.getCover() != null) {
			entity.setCover(dto.getCover());
		}
		if(dto.getCoverSmall() != null) {
			entity.setCoverSmall(dto.getCoverSmall());
		}
	}

	private void validateCategoryParent(CategoryDTO categoryJson) {
		if (categoryJson.getParentId() != null) {
			if (!categoryRepository.existsById(categoryJson.getParentId())) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, CAT$0001, categoryJson.getParentId());
			}
		}
	}

    private void validateCategoryName(CategoryDTO categoryJson) {
		if (categoryJson.getName() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "name");
		} else if (!StringUtils.validateName(categoryJson.getName())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, categoryJson.getName());
		}
	}
    
    
    @Override
		@CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID, ORGANIZATIONS_TAG_TREES})
    public CategoriesEntity updateCategory(CategoryDTO categoryJson) {
        CategoriesEntity category = ofNullable(categoryJson.getId())
				.map(categoryRepository::findById)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, CAT$0002));
        if (categoryJson.getName() != null) {
            validateCategoryName(categoryJson);
			category.setName(categoryJson.getName());
			category.setPname(StringUtils.encodeUrl(categoryJson.getName()));
        }
        setCategoryEntityAdditionalInfo(category, categoryJson);
		return categoryRepository.save(category);
    }


    @Override
		public CategoryResponse deleteCategory(Long categoryId) throws BusinessException {
		if (categoryId == null ){
			throw new BusinessException("MISSING_PRARM: Category_id", "",HttpStatus.NOT_ACCEPTABLE);
		}

        CategoriesEntity categoriesEntity = getCategoryById(categoryId);

        List<Long> tagsIds = orgTagsRepo.findByCategoryId(categoryId);
        if (tagsIds.size() > 0){
			throw new RuntimeBusinessException(CONFLICT, GEN$0017, "tags", tagsIds.toString());
        }

        List<Long> childrenCategoriesIds = categoryRepository.findCategoriesIdsByParentId(categoryId);
        if (childrenCategoriesIds.size() > 0){
            throw new RuntimeBusinessException(CONFLICT, GEN$0017, "children categories", childrenCategoriesIds.toString());
        }

		List<Long> productsIds = productRepository.findProductsIdsByCategoryId(categoryId);
		if (productsIds.size() > 0){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0017, "products", productsIds.toString());
		}
        categoryRepository.delete(categoriesEntity);
        return new CategoryResponse(categoriesEntity.getId());
    }





//    @CacheResult(cacheName = "organizations_tags")
    @Override
		public List<TagsRepresentationObject> getOrganizationTags(Long orgId, String categoryName) {
		List<TagsEntity> tagsEntities;
		if (isBlankOrNull(categoryName)) {
			tagsEntities = orgTagsRepo.findByOrganizationEntity_IdOrderByPriorityDesc(orgId);
		} else {
			tagsEntities = orgTagsRepo.findByCategoriesEntity_NameAndOrganizationEntity_IdOrderPriorityDesc(categoryName, orgId);
		}
		return toTagsDTO(tagsEntities);
	}

	@Override
	public PageImpl<TagsRepresentationObject> getYeshteryOrganizationsTags(Integer start, Integer count , String categoryName, Long orgId) {
		Pageable page = new CustomOffsetAndLimitPageRequest(start, count);

		Set<Long> orgIdList = new HashSet<>();
    	if (orgId == null) {
			orgIdList = orgRepo.findIdByYeshteryState(1);
		} else {
			orgIdList = orgRepo.findIdByYeshteryStateAndOrganizationId(1, orgId);
		}
		PageImpl<TagsEntity> tagsEntities;
		if(isBlankOrNull(categoryName)) {
			tagsEntities = orgTagsRepo.findByOrganizationEntity_IdInOrderByPriorityDesc(orgIdList ,page);
		} else {
			tagsEntities = orgTagsRepo.findByCategoriesEntity_NameAndOrganizationEntity_IdInOrderByPriorityDesc(categoryName, orgIdList,page);
		}
		return new PageImpl<>(toTagsDTO(tagsEntities.getContent()), tagsEntities.getPageable(), tagsEntities.getTotalElements());

	}

	private List<TagsRepresentationObject> toTagsDTO(List<TagsEntity> tagsEntities) {
		return tagsEntities
				.stream()
				.map(tag ->(TagsRepresentationObject) tag.getRepresentation())
				.collect(toList());
    }
    
    
    
    @Override
		@CacheResult(cacheName = ORGANIZATIONS_TAG_TREES)
    public List<TagsTreeNodeDTO> getOrganizationTagsTree(Long orgId) throws BusinessException {

        OrganizationEntity org = orgRepo.getOne(orgId);
        if (org == null)
            throw new BusinessException(
            		"Provided org_id doesn't match any existing organization"
                    , "INVALID_PARAM: org_id"
            		, NOT_ACCEPTABLE);
        
        Graph<TagGraphNodeEntity, DefaultEdge> graph = buildTagGraph(orgId);
        
        return convertToTagTree(graph);
    }





	private List<TagsTreeNodeDTO> convertToTagTree(Graph<TagGraphNodeEntity, DefaultEdge> graph) {
		BreadthFirstIterator<TagGraphNodeEntity, DefaultEdge>  iterator = 
        	      new BreadthFirstIterator<TagGraphNodeEntity ,DefaultEdge>(graph);
        
        Map<Long, TagsTreeNodeDTO> nodeMap = new HashMap<>();
        List<TagsTreeNodeDTO> rootNodes = new ArrayList<>();
        while(iterator.hasNext()) {
        	TagGraphNodeEntity nodeEntity = iterator.next();        	
        	addNodeToTree(graph, nodeMap, rootNodes, nodeEntity);        	
        }
		return rootNodes;
	}





	private void addNodeToTree(Graph<TagGraphNodeEntity, DefaultEdge> graph, Map<Long, TagsTreeNodeDTO> nodeMap,
			List<TagsTreeNodeDTO> rootNodes, TagGraphNodeEntity nodeEntity) {
		Set<TagGraphNodeEntity> parentNodesEntities =  getTagTreeNodeParents(graph, nodeEntity);
		parentNodesEntities.forEach(parent -> addNodeToTree(graph, nodeMap, rootNodes, parent));
		
		if(nodeMap.containsKey(nodeEntity.getId())) {
			return;	//already processed
		}
		
		TagsTreeNodeDTO node = toTagsTreeNodeDTO(nodeEntity);
		nodeMap.put(node.getNodeId(), node);		
		
		if(parentNodesEntities.isEmpty()) {
			rootNodes.add(node);
		}else {
			parentNodesEntities.forEach(parent -> addTagTreeNodeUnderParent(parent, node, nodeMap));        		
		}
	}





	private Set<TagGraphNodeEntity> getTagTreeNodeParents(Graph<TagGraphNodeEntity, DefaultEdge> graph,
			TagGraphNodeEntity nodeEntity) {
		return graph
		.incomingEdgesOf(nodeEntity)
		.stream()
		.map(graph::getEdgeSource)
		.filter(Objects::nonNull)
		.collect(toSet());
	}





	private void addTagTreeNodeUnderParent(TagGraphNodeEntity parentNodeEntity, TagsTreeNodeDTO node , Map<Long, TagsTreeNodeDTO> nodeMap) {
		ofNullable(parentNodeEntity)
		  .map(TagGraphNodeEntity::getId)
		  .map(nodeMap::get)
		  .map(TagsTreeNodeDTO::getChildren)
		  .orElse(new ArrayList<>())
		  .add(node);
	}





	private Graph<TagGraphNodeEntity, DefaultEdge> buildTagGraph(Long orgId) {
		List<TagGraphNodeEntity> nodes = tagNodesRepo.findByTag_OrganizationEntity_Id(orgId);
        List<TagGraphEdgesEntity> edges = tagEdgesRepo.findByOrganizationId(orgId);
        
        Graph<TagGraphNodeEntity, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        nodes.forEach(graph::addVertex);
        edges.forEach(edge -> graph.addEdge(edge.getParent(), edge.getChild()));
        
		return graph;
	}

    

	@Override
	public TagResponse createOrUpdateTagThroughApi(TagsDTO tagDTO) throws BusinessException {
		tagDTO.setHasCategory(true);
		TagsEntity tag = createOrUpdateTag(tagDTO);
		return new TagResponse(tag.getId());
	}


	@Override
	@CacheEvict(allEntries = true ,cacheNames = ORGANIZATIONS_TAG_TREES)
    public TagsEntity createOrUpdateTag(TagsDTO tagDTO) throws BusinessException{
        validateTagDto(tagDTO);
        
        String operation = tagDTO.getOperation();
        
        TagsEntity entity = null;
        if(Objects.equals(operation, "create")) {
        	entity = createNewTag(tagDTO);
        }else {
        	entity = updateTag(tagDTO);
        }    

        return entity;
    }





	private TagsEntity updateTag(TagsDTO tagDTO)
			throws BusinessException{
		TagsEntity entity;
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		entity = orgTagsRepo
					.findByIdAndOrganizationEntity_Id(tagDTO.getId(), org.getId())
					.orElseThrow(() -> new BusinessException(
											"INVALID PARAM: id"
											, "No tag exists in the organization with provided id"
											, NOT_ACCEPTABLE));
		
		try {
			copyNonNullProperties(tagDTO, entity);			
			setCategory(tagDTO, entity);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error(e, e);
			throw new BusinessException(
					format("Failed to update Tag [%s]!", tagDTO.toString())
					, "INTERNAL FAILURE"
					, INTERNAL_SERVER_ERROR);
		}

		if (tagDTO.isUpdated("graphId")) {
			Integer graphId = tagDTO.getGraphId() != null? org.getId().intValue() : null;
			entity.setGraphId(graphId);            	
		}
		return orgTagsRepo.save(entity);
	}




	private void setCategory(TagsDTO tagDTO, TagsEntity entity) throws BusinessException {
		Long categoryId = tagDTO.getCategoryId();
		if(Objects.nonNull(categoryId)) {
			CategoriesEntity category = 
					categoryRepository
						.findById(categoryId)
						.orElseThrow(() -> 
							new BusinessException(
								format("No category exists with id [%d]!", categoryId)
								, "INVALID PARAM: category_id"
								, NOT_ACCEPTABLE));
			entity.setCategoriesEntity(category);
		}
	}





	private void validateTagDto(TagsDTO tagDTO) throws BusinessException {
		String operation = tagDTO.getOperation();
        Long categoryId = tagDTO.getCategoryId();
        boolean hasCategory = tagDTO.isHasCategory();
        
        if( isBlankOrNull(operation)) {
        	throw new BusinessException("MISSING PARAM: operation", "", NOT_ACCEPTABLE);
        }else if(Objects.equals(operation, "create")) {
        	if (categoryId == null && hasCategory) {
            	throw new BusinessException("MISSING PARAM: category_id", "category_id is required to create tag", NOT_ACCEPTABLE);
            }else if (isNull(tagDTO.getName())) {
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
    	CategoriesEntity category = getCategoryEntity(tagDTO);
    	String alias = ofNullable(tagDTO.getAlias()).orElse(tagDTO.getName());
       
    	TagsEntity entity = new TagsEntity();
        entity.setOrganizationEntity(org);
        entity.setCategoriesEntity(category);
        entity.setName(tagDTO.getName());
        entity.setAlias(alias);        
        entity.setGraphId(tagDTO.getGraphId());
        entity.setMetadata(tagDTO.getMetadata());
        if(tagDTO.getGraphId() != null) {
        	entity.setGraphId(org.getId().intValue()); // TODO will change to tagDTO.getGraphId() when we support MultiGraph per org
        }
		if(tagDTO.getPriority() != null) {
			entity.setPriority(tagDTO.getPriority());
		} else {
			entity.setPriority(0);
		}
        
        entity = orgTagsRepo.save(entity);
        String pname = format("%d-%s", entity.getId(), encodeUrl(tagDTO.getName()));
        entity.setPname(pname);
        
        return orgTagsRepo.save(entity);
    }

 



	private CategoriesEntity getCategoryEntity(TagsDTO tagDTO) {
		return 
			ofNullable(tagDTO)
			.filter(TagsDTO::isHasCategory)
			.map(TagsDTO::getCategoryId)
			.map(this::findCategoryById)
			.orElse(null);
	}




	private CategoriesEntity findCategoryById(Long id) {
		return categoryRepository
				.findById(id)
				.orElseThrow(() -> new RuntimeBusinessException("INVALID PARAM: category_id", "No category exists with provided id", NOT_ACCEPTABLE));
	}
    
    

    
    @Override
		@Transactional(rollbackFor = Throwable.class)
//    @CacheRemove(cacheName = "organizations_tag_trees", cacheKeyGenerator = CurrentOrgCacheKeyGenerator.class)
    //TODO: should remove the value for the current organization only, but i couldn't implementa KeyGenerator that generate a matching key for  
    //the existing one, need to understand how the key is generated when calling getTagTree, or just use spring cache annotations.
    @CacheRemoveAll(cacheName = "organizations_tag_trees") 
    public void createTagTree(TagsTreeCreationDTO tagsTreeDTO) throws BusinessException{

        List<TagsTreeNodeCreationDTO> tree = tagsTreeDTO.getTreeNodes();

        if(tree == null) {
        	return;
        }
        
        Map<Long, TagsEntity> tagsCache = createTagsCache();
        Map<Long, TagGraphNodeEntity> tagsNodesCache = createTagsNodesCache();

		clearTagsTreeEdges();
        Set<Long> usedNodes = buildNewTagGraph(tree, tagsCache, tagsNodesCache);
		clearUnusedTagsNodes(usedNodes);
    }


    private Map<Long, TagGraphNodeEntity> createTagsNodesCache() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return
				tagNodesRepo
						.findByTag_OrganizationEntity_Id(orgId)
						.stream()
						.collect(
								toMap(TagGraphNodeEntity::getId, tag -> tag));
	}




	private Set<Long> buildNewTagGraph(List<TagsTreeNodeCreationDTO> tree, Map<Long, TagsEntity> tagsMap,
								  Map<Long, TagGraphNodeEntity> tagsNodesCache) {
		return tree
				.stream()
				.map(node -> createTagSubTree(node, tagsMap, tagsNodesCache))
				.map(TagGraphNodeEntity::getId)
				.collect(toSet());
	}



	
	
	private TagGraphNodeEntity createTagSubTree(TagsTreeNodeCreationDTO node, Map<Long, TagsEntity> tagsMap,
												Map<Long, TagGraphNodeEntity> tagsNodesCache) {
		List<TagGraphNodeEntity> childrenNodes = createSubTreesForChildrenNodes(node, tagsMap, tagsNodesCache);
		TagGraphNodeEntity	savedNode = createTagTreeNode(node, tagsMap, tagsNodesCache);

		removeDuplicateTagsInTheSameLevel(childrenNodes);

		childrenNodes
		 .stream()
		 .map(child -> new TagGraphEdgesEntity(savedNode, child))
		 .forEach(tagEdgesRepo::save);
		
		return savedNode;
	}


	private void removeDuplicateTagsInTheSameLevel(List<TagGraphNodeEntity> childrenNodes) {
		Set<TagsEntity> uniqueTags = new HashSet<>();
		List<TagGraphNodeEntity> nodesToCheck = new ArrayList<>(childrenNodes);
		if (childrenNodes != null && !childrenNodes.isEmpty()) {
			for (TagGraphNodeEntity n : nodesToCheck) {
				if (uniqueTags.contains(n.getTag())) {
					childrenNodes.remove(n);
				}
				else {
					uniqueTags.add(n.getTag());
				}
			}
		}
	}


	private TagGraphNodeEntity createTagTreeNode(TagsTreeNodeCreationDTO node, Map<Long, TagsEntity> tagsMap,
												  Map<Long, TagGraphNodeEntity> tagsNodesCache) {
		return ofNullable(node)
				.map(TagsTreeNodeCreationDTO::getNodeId)
				.map(id -> tagsNodesCache.get(id))
				.map(n -> checkNodeOriginalTag(n, node, tagsMap))
				.orElseGet(() -> persistTagTreeNode(node, tagsMap));
	}



	private TagGraphNodeEntity persistTagTreeNode(TagsTreeNodeCreationDTO node, Map<Long, TagsEntity> tagsMap) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return ofNullable(node)
				.map(TagsTreeNodeCreationDTO::getTagId)
				.map(tagsMap::get)
				.map(this::verifyTagToBeAddedToTree)
				.map(TagGraphNodeEntity::new)
				.map(tagNodesRepo::save)
				.orElseThrow(() ->
						new RuntimeBusinessException(
								format("Failed to create Tree node for tag with id[%d]! Maybe it doesn't exists for organization[%d]!", node.getTagId(), orgId)
								, "INVALID PARAM: nodes"
								, NOT_ACCEPTABLE));
	}



	private TagGraphNodeEntity checkNodeOriginalTag(TagGraphNodeEntity nodeEntity, TagsTreeNodeCreationDTO dto, Map<Long, TagsEntity> tagsMap) {
    	Long tagId = dto.getTagId();
    	if (!nodeEntity.getTag().getId().equals(tagId)) {
    		TagsEntity tag =
					ofNullable(tagsMap.get(tagId))
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, TAG$TREE$0001,tagId));
			verifyTagToBeAddedToTree(tag);
    		nodeEntity.setTag(tag);
		}
    	return tagNodesRepo.save(nodeEntity);
	}


	
	private TagsEntity verifyTagToBeAddedToTree(TagsEntity tag) {
		if(tag.getCategoriesEntity() == null) {
			throw new RuntimeBusinessException(
					format("Cannot add tag with id[%d] to the tag tree! Tag doesnot have a category!", tag.getId())
					, "INVALID PARAM: nodes"
					, NOT_ACCEPTABLE);
		}
		return tag;
	}




	private List<TagGraphNodeEntity> createSubTreesForChildrenNodes(TagsTreeNodeCreationDTO node,
											Map<Long, TagsEntity> tagsMap, Map<Long, TagGraphNodeEntity> tagsNodesCache) {
		return ofNullable(node)
				.map(TagsTreeNodeCreationDTO::getChildren)
				.orElse(emptyList())
				.stream()
				.filter(child -> noneIsNull(child))
				.map(child -> createTagSubTree(child, tagsMap, tagsNodesCache))
				.collect(toList());
	}
	
	

	

	private Map<Long, TagsEntity> createTagsCache() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return 
			orgTagsRepo
				.findByOrganizationEntity_Id(orgId)
				.stream()
				.collect(
						toMap(TagsEntity::getId, tag -> tag));
	}

	
	
	
    
    
    @Override
		public TagResponse deleteOrgTag(Long tagId) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();
        TagsEntity tag = 
        		orgTagsRepo
					.findByIdAndOrganizationEntity_Id(tagId, orgId)
					.orElseThrow(() -> 
						new BusinessException(
							"Provided tag_id doesn't match any existing tag"
							,"INVALID_PARAM: tag_id"
							, NOT_ACCEPTABLE));

        validateTagToDelete(tagId);
            
        productRepository.detachProductsFromTag(tagId);

        orgTagsRepo.delete(tag);

        return new TagResponse(tag.getId());
    }





	private void validateTagToDelete(Long tagId) throws BusinessException {
		List<TagGraphNodeEntity> tags = tagNodesRepo.findByTag_Id(tagId);
        if (!tags.isEmpty()) {
        	throw new BusinessException(
            		format("Tag[%d] Exists in the tag tree!", tagId)
            		,"NOT_EMPTY:tags Links"
            		, NOT_ACCEPTABLE);
        }
	}

    
    
    
    
    private void clearTagsTreeEdges() {
    	Long orgId = securityService.getCurrentUserOrganizationId();
        
    	List<TagGraphEdgesEntity> tagsEdges = tagEdgesRepo.findByOrganizationId(orgId);
        tagEdgesRepo.deleteAll(tagsEdges);
        tagEdgesRepo.flush();
    }


    private void clearUnusedTagsNodes( Set<Long> usedNodes) {
		Long orgId = securityService.getCurrentUserOrganizationId();

    	Set<Long> allUsedNodes = tagNodesRepo.findUsedTagsNodes(orgId)
				.stream()
				.map(BigInteger::longValue)
				.collect(toSet());
    	allUsedNodes.addAll(usedNodes);
    	if (!allUsedNodes.isEmpty()) {
			tagNodesRepo.deleteByIdNotIn(allUsedNodes, orgId);
		} else {
    		tagNodesRepo.deleteByOrgId(orgId);
		}
	}
    
    
    private TagsTreeNodeDTO toTagsTreeNodeDTO(TagGraphNodeEntity nodeEntity) {
    	TagsTreeNodeDTO dto = new TagsTreeNodeDTO();    	
    	TagsEntity tagEntity = ofNullable(nodeEntity.getTag()).orElse(new TagsEntity());
    	
    	dto.setNodeId(nodeEntity.getId());
    	dto.setTagId(tagEntity.getId());
    	dto.setId(tagEntity.getId());
		dto.setOrgId(tagEntity.getOrganizationEntity().getId());
		dto.setAlias(tagEntity.getAlias());
    	dto.setCategoryId(tagEntity.getCategoriesEntity().getId());
    	dto.setGraphId(tagEntity.getGraphId());    	
    	dto.setMetadata(tagEntity.getMetadata());
    	dto.setName(tagEntity.getName());
    	dto.setPname(tagEntity.getPname());    	
    	return dto;
    }


    @Override
		public void assignTagsCategory(Long categortId, List<Long> tagsIds) throws BusinessException {
    	Long orgId = securityService.getCurrentUserOrganizationId();

    	if (categortId == null)
    		throw new BusinessException(null, "MISSING_PARAM: category_id", NOT_ACCEPTABLE);

    	if (tagsIds == null || tagsIds.isEmpty())
    		orgTagsRepo.setAllTagsCategory(categortId, orgId);
		else
			orgTagsRepo.setTagsListCategory(categortId, orgId, tagsIds);
	}


	@Override
	public void setTagsListCategory(UpdateTagsCategoryDTO updateDto) {
    	var categoryId = updateDto.getCategoryId();
    	var tagsIds = updateDto.getTagsIds();
    	CategoriesEntity category = getCategoryById(categoryId);
    	orgTagsRepo.setTagsListCategory(category, tagsIds);
	}



	@Override
	public void setProductsListCategory(UpdateProductsCategoryDTO updateDto) {
		var categoryId = updateDto.getCategoryId();
		var productsIds = updateDto.getProductsIds();
		getCategoryById(categoryId);
		productRepository.setProductsListCategory(categoryId, productsIds);
	}

	private CategoriesEntity getCategoryById(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, GEN$0016, categoryId));
	}


	@Override
	public List<TagsRepresentationObject> findCollections(String name, Long orgId) {
		return orgTagsRepo.findByNameAndOrganizationIdAndCategoryId(name.toLowerCase(), orgId)
				.stream()
				.map(t -> (TagsRepresentationObject) t.getRepresentation())
				.collect(toList());
	}



	@Override
	public List<CategoryDto> getCategoriesTree() {
		var categories = categoryRepository.findAll();
    	var cache =
				categories.stream()
						.collect(
							toUnmodifiableMap(ent -> ofNullable(ent.getId()).orElse(DUMMY_ROOT_PARENT)
									, Function.identity()
									, FunctionalUtils::getFirst));
    	return cache.values()
				.stream()
				.collect(
					collectingAndThen(
						groupingBy(ent -> ofNullable(ent.getParentId()).orElse(DUMMY_ROOT_PARENT))
							, children -> createCategoriesTree(children, cache)
					));
	}


	private List<CategoryDto> createCategoriesTree(Map<Long,List<CategoriesEntity>> categoriesChildren
			, Map<Long,CategoriesEntity> allCategoriesCache) {
    	var allDtoCache =
				allCategoriesCache.entrySet()
						.stream()
						.map(e -> Map.entry(e.getKey(), toCategoryDto(e.getValue())))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, FunctionalUtils::getFirst));
    	addChildrenToCachedDtos(allDtoCache, categoriesChildren);
		return allDtoCache
				.values()
				.stream()
				.filter(dto -> isNull(dto.getParent()))
				.collect(toList());
	}



	private void addChildrenToCachedDtos(Map<Long, CategoryDto> allDtoCache, Map<Long, List<CategoriesEntity>> categoriesChildren) {
    	allDtoCache
				.values()
				.stream()
				.filter(dto -> categoriesChildren.containsKey(dto.getId()))
				.forEach(dto -> {
					var children =
							categoriesChildren.get(dto.getId())
									.stream()
									.map(entity -> allDtoCache.get(entity.getId()))
									.collect(toList());
					dto.setChildren(children);
				});
	}


	private CategoryDto toCategoryDto(CategoriesEntity entity) {
    	var dto = new CategoryDto();
    	dto.setName(entity.getName());
    	dto.setPname(entity.getPname());
    	dto.setLogo(entity.getLogo());
    	dto.setCover(entity.getCover());
    	dto.setCoverSmall(entity.getCoverSmall());
    	dto.setId(entity.getId());
    	dto.setParent(entity.getParentId());
    	return dto;
	}

	@Override
	public List<TagsRepresentationObject> getYeshteryTags(String categoryName) {
		if (securityService.getYeshteryState() == 1) {
			List<TagsEntity> tagsEntities;
			tagsEntities = orgTagsRepo.findByCategoriesEntity_NameOrderByName(categoryName);
			return tagsEntities
					.stream()
					.map(tag -> (TagsRepresentationObject) tag.getRepresentation())
					.collect(toList());
		}
		return null;
	}

	@Override
	@CacheResult(cacheName = ORGANIZATIONS_TAG_TREES)
	public List<TagsTreeNodeDTO> getYeshteryTagsTree() throws BusinessException {

		Graph<TagGraphNodeEntity, DefaultEdge> graph = buildYeshteryTagGraph();

		return convertToTagTree(graph);
	}

	private Graph<TagGraphNodeEntity, DefaultEdge> buildYeshteryTagGraph() {
		List<TagGraphNodeEntity> nodes = tagNodesRepo.findByTag();
		List<TagGraphEdgesEntity> edges = tagEdgesRepo.findAllYeshteryTagGraph(1);

		Graph<TagGraphNodeEntity, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		nodes.forEach(graph::addVertex);
		edges.forEach(edge -> graph.addEdge(edge.getParent(), edge.getChild()));

		return graph;
	}

}


