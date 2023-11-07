package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SearchType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.CategoryService;
import com.nasnav.service.DataExportService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SearchService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.TriFunction;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.SpringUtils.readOptionalResource;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.Roles.ORGANIZATION_ADMIN;
import static com.nasnav.enumerations.SearchType.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.action.ActionListener.wrap;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.common.unit.Fuzziness.AUTO;
import static org.elasticsearch.common.xcontent.XContentType.JSON;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service
public class SearchServiceImpl implements SearchService {

    private final static Logger logger = LogManager.getLogger();

    private static final int MAX_PG_SIZE = 100;
    private static final int DEFAULT_PG_SIZE = 10;
    private static final String COMMON_MAPPING_JSON = "classpath:/json/search/common_index_mapping.json";

    @Autowired
    RestHighLevelClient client;

    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataExportService exportService;

    @Autowired
    private ProductService productService;

    @Value(COMMON_MAPPING_JSON)
    private Resource commonMappingResource;

    @Override
    public Mono<SearchResult> search(SearchParameters parameters, boolean onlyYeshtery) {
        if(anyIsNull(parameters, parameters.keyword)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, NAVBOX$SRCH$0001);
        }

        var normalizedParams = createNormalizedParams(parameters, onlyYeshtery);
        var searchSourceBuilder = getSearchSourceBuilder(normalizedParams);
        var searchSuggestionBuilder = getSuggestionSourceBuilder(normalizedParams);

        var indices = getIndices(normalizedParams);
        var searchRequest = getSearchRequest(searchSourceBuilder, indices);
        var suggestionRequest = getSearchRequest(searchSuggestionBuilder, indices);
        return Mono
                .<SearchResult>create(sink -> searchAsync(sink, searchRequest))
                .flatMap(response -> addSuggestionToResponse(response, suggestionRequest))
                .doOnError(e -> logger.error(e,e));
    }



    private Mono<SearchResult> addSuggestionToResponse(SearchResult response, SearchRequest suggestionRequest) {
        return Mono
                .create(sink -> getSuggestionAsync(sink, suggestionRequest, response));
    }




    private SearchRequest getSearchRequest(SearchSourceBuilder searchSourceBuilder, String[] indices) {
        var searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        if(indices.length > 0){
            searchRequest.indices(indices);
        }
        return searchRequest;
    }



    private SearchSourceBuilder getSuggestionSourceBuilder(NormalizedSearchParameters params) {
        var mainQuery = QueryBuilders
                .boolQuery()
                .should( regexpQuery("name", ".*"+params.keyword+".*"))
                .should( fuzzyQuery("name", params.keyword))
                .minimumShouldMatch(1);      //at least one condition should be met
        if(nonNull(params.org_id)){
            mainQuery.filter( matchQuery("organization_id", params.org_id));
        }
        if(params.only_yeshtery){
            mainQuery.filter(matchQuery("yeshtery_state", 1));
        }
        return new SearchSourceBuilder()
                .query( mainQuery)
                .fetchSource(new String[]{"name"}, new String[]{})
                .from(params.start)
                .size(params.count);
    }



    @Override
    public Mono<Void> syncSearchData() {
        var organizationsToSync = getOrgsToSync();
        return Flux
                .fromIterable(organizationsToSync)
                .flatMap(this::doSyncSearchData)
                .reduce((res1, res2) -> res2);
    }



    @Override
    public Mono<Void> deleteAllIndices() {
        return deleteIndex(TAGS)
                .then(deleteIndex(PRODUCTS))
                .then(deleteIndex(COLLECTIONS));
    }



    private Mono<Void> deleteIndex(SearchType index) {
        var indexName = getIndex(index);
        var deleteRequest = new DeleteIndexRequest(indexName);
        return indexExists(indexName)
                .flatMap(exists ->
                        exists? runWithDefaultParams(client.indices()::deleteAsync, deleteRequest, AcknowledgedResponse.class)
                                : Mono.create(MonoSink::success));
    }


    private NormalizedSearchParameters createNormalizedParams(SearchParameters parameters, boolean onlyYeshtery) {
        var normalized = new NormalizedSearchParameters();
        normalized.keyword = ofNullable(parameters.keyword).orElse("").toLowerCase();
        normalized.org_id = parameters.org_id;
        normalized.type = parameters.type;
        if (normalized.keyword.isEmpty() || normalized.keyword.isBlank()) {
            normalized.type = Arrays.asList(SearchType.TAGS);
        }
        normalized.start = ofNullable(parameters.start).orElse(0);
        normalized.count = ofNullable(parameters.count).map(this::limitPage).orElse(DEFAULT_PG_SIZE);
        normalized.only_yeshtery = onlyYeshtery;
        return normalized;
    }




    private List<OrganizationEntity> getOrgsToSync() {
        if(securityService.currentUserHasRole(NASNAV_ADMIN)){
            return orgRepo.findAllOrganizations();
        }
        else if(securityService.currentUserHasRole(ORGANIZATION_ADMIN)){
            var orgId = securityService.getCurrentUserOrganization();
            return singletonList(orgId);
        }else{
            return emptyList();
        }
    }



    private Mono<Void> doSyncSearchData(OrganizationEntity org){
        return deleteOrganizationData(org)
                .then(createIndicesIfNeeded())
                .then(resendOrganizationData(org));
    }



    private Mono<Void> createIndicesIfNeeded() {
        return createProductsIndex()
                .then(createCollectionsIndex())
                .then(createTagsIndex());
    }



    private Mono<Void> createIndex(String index) {
        var request = new CreateIndexRequest(index);
        request.mapping(getCommonIndexMapping(), JSON);
        return indexExists(index)
                .flatMap(exists ->
                            exists? Mono.create(MonoSink::success)
                                    : runWithDefaultParams(client.indices()::createAsync, request, CreateIndexResponse.class));
    }


    private Mono<Void> createTagsIndex() {
        return createIndex(getIndex(TAGS));
    }


    private Mono<Void> createCollectionsIndex() {
        return createIndex(getIndex(COLLECTIONS));
    }

    

    private Mono<Void> createProductsIndex() {
        return createIndex(getIndex(PRODUCTS));
    }



    private String getCommonIndexMapping(){
        return readOptionalResource(commonMappingResource)
                .orElseThrow(()-> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0019, COMMON_MAPPING_JSON));
    }



    private Mono<Void> deleteOrganizationData(OrganizationEntity org){
        var orgId = org.getId();
        return Mono
                .zip(deleteProductsIndexData(orgId)
                        , deleteCollectionsIndexData(orgId)
                        , deleteTagsIndexData(orgId))
                .then();
    }



    private Mono<Void> deleteTagsIndexData(Long orgId) {
        return deleteIndexOfNameAndOrganization(getIndex(TAGS), orgId);
    }



    private Mono<Void> deleteCollectionsIndexData(Long orgId) {
        return deleteIndexOfNameAndOrganization(getIndex(COLLECTIONS), orgId);
    }



    private Mono<Void> deleteProductsIndexData(Long orgId) {
        return deleteIndexOfNameAndOrganization(getIndex(PRODUCTS), orgId);
    }



    private Mono<Void> deleteIndexOfNameAndOrganization(String name, Long orgId) {
        return indexExists(name)
                .flatMap(exists -> exists? doDeleteIndexOfNameAndOrganization(name, orgId): Mono.create(MonoSink::success));
    }



    private Mono<Void> doDeleteIndexOfNameAndOrganization(String name, Long orgId) {
        var request = new DeleteByQueryRequest(name);
        request.setQuery(new TermQueryBuilder("organization_id", orgId));
        return runWithDefaultParams(client::deleteByQueryAsync, request, BulkByScrollResponse.class);
    }



    private <T,R extends ActionResponse> Mono<Void> runWithDefaultParams(TriFunction<T, RequestOptions, ActionListener<R>, ?> fun, T request, Class<R> responseType){
        return Mono
                .<Void>create(sink -> fun.apply(request, DEFAULT, defaultCallBack(sink)) )
                .doOnError(e -> logger.error(e,e));
    }



    private  <R extends ActionResponse> ActionListener<R> defaultCallBack(MonoSink<Void> sink) {
        return ActionListener.wrap((res) -> sink.success(), sink::error);
    }



    private Mono<Boolean> indexExists(String indexName) {
        return Mono
                .create(sink ->
                            client
                                .indices()
                                .existsAsync(
                                        new GetIndexRequest(indexName)
                                        , DEFAULT
                                        , wrap(sink::success, sink::error)));
    }



    private Mono<Void> resendOrganizationData(OrganizationEntity org){
        return Mono
                .zip(sendTagsData(org), sendProductsAndCollectionsData(org))
                .then();
    }



    private Mono<Void> sendTagsData(OrganizationEntity org) {
        var orgId = org.getId();
        var request = new BulkRequest();
        categoryService
                .getOrganizationTags(orgId, null)
                .stream()
                .map(tag -> new TagsObject(tag, org))
                .map(tag -> createIndexRequest(TAGS, tag))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(request::add);
       return Mono
               .<Void>create(sink -> client.bulkAsync(request, DEFAULT, wrap((res)-> handleBulkResponse(res, sink, orgId), sink::error)))
               .doOnError(e -> logger.error(e,e));
    }




    private void handleBulkResponse(BulkResponse response, MonoSink<Void> sink, Long orgId){
        if(!response.hasFailures()){
            sink.success();
        }else{
            var errMsg = response.buildFailureMessage();
            Throwable e = new RuntimeBusinessException(NOT_ACCEPTABLE, SRCH$SYNC$0002, orgId, errMsg);
            sink.error(e);
        }

    }



    private <T> Optional<IndexRequest> createIndexRequest(SearchType type ,T obj){
        var index = getIndex(type);
        var request = new IndexRequest(index);
        try {
            var json = objectMapper.writeValueAsString(obj);
            request.source(json, JSON);
            return Optional.of(request);
        } catch (JsonProcessingException e) {
            logger.error(e,e);
            return empty();
        }
    }




    private Mono<Void> sendProductsAndCollectionsData(OrganizationEntity org) {
        var orgId = org.getId();
        var request = new BulkRequest();
        var extraData = getProductsExtraData(orgId);
        var totalProducts = getTotalProducts(orgId);
        double batchSize = 1000;
        var batches = (int)Math.ceil(totalProducts/batchSize);
        logger.info(format("Sync search data for org[%d]: sync [%d] product and collections in [%d] batches!", orgId, totalProducts, batches));
        for(var i = 0; i< batches; i++ ){
            try {
                var products = getProductsBatch(org, extraData, batchSize, i);
                addProductsToBulkIndexRequest(request, products);
                addCollectionsToBulkIndexRequest(request, products);
            } catch (Throwable e) {
                logger.error(e,e);
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, SRCH$SYNC$0001, orgId);
            }
        }
        return Mono
                .just(request)
                .filter(req -> req.numberOfActions() > 0)
                .flatMap(req ->
                        Mono.<Void>create(sink -> client.bulkAsync(req, DEFAULT, wrap((res)-> handleBulkResponse(res, sink, orgId), sink::error)))
                )
                .doOnError(e -> logger.error(e,e));
    }




    private Map<Long, List<CsvRow>> getProductsExtraData(Long orgId) {
        return exportService
                .exportProductsData(orgId, null)
                .stream()
                .collect(groupingBy(CsvRow::getProductId));
    }




    private void addCollectionsToBulkIndexRequest(BulkRequest request, List<Product> products) {
        products
            .stream()
            .filter(product -> product.getProductType() == 2)
            .map(product -> createIndexRequest(COLLECTIONS, product))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(request::add);
    }



    private void addProductsToBulkIndexRequest(BulkRequest request, List<Product> products) {
        products
            .stream()
            .filter(product -> product.getProductType() == 0)
            .map(product -> createIndexRequest(PRODUCTS, product))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(request::add);
    }



    private List<Product> getProductsBatch(OrganizationEntity org, Map<Long, List<CsvRow>> extraData, double batchSize, int i) throws BusinessException {
        var start = (int)(batchSize * i);
        var params = new ProductSearchParam();
        params.org_id = org.getId();
        params.count = (int) batchSize;
        params.start = start;
        return productService
                .getProducts(params)
                .getProducts()
                .stream()
                .map(prod -> new Product(prod, extraData.get(prod.getId()), org))
                .collect(toList());
    }



    private Long getTotalProducts(Long orgId) {
        var params = new ProductSearchParam();
        params.org_id = orgId;
        params.count = 10;
        params.start = 0;
        try {
            return  productService.getProducts(params).getTotal();
        } catch (BusinessException e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SRCH$SYNC$0001, orgId);
        }
    }



    private String[] getIndices(SearchParameters parameters) {
        return ofNullable(parameters.type)
                .orElse(emptyList())
                .stream()
                .map(SearchType::name)
                .map(String::toLowerCase)
                .toArray(String[]::new);
    }



    private SearchSourceBuilder getSearchSourceBuilder(NormalizedSearchParameters parameters) {
        var mainQuery =
                QueryBuilders
                .boolQuery()
                .must( multiMatchQuery(parameters.keyword)
                        .fuzziness(AUTO)
                        .field("name", 3) //give priority to field "name"
                        .field("*"));
        if(nonNull(parameters.org_id)){
            mainQuery.filter(matchQuery("organization_id", parameters.org_id));
        }
        if(parameters.only_yeshtery){
            mainQuery.filter(matchQuery("yeshtery_state", 1));
        }
        return new SearchSourceBuilder()
                .query(mainQuery)
                .from(parameters.start)
                .size(parameters.count);
    }



    private void searchAsync(MonoSink<SearchResult> sink, SearchRequest searchRequest){
        client.searchAsync(
                searchRequest
                , DEFAULT
                , ActionListener.wrap((res)-> emitSearchResponse(sink, res), sink::error));
    }



    private void getSuggestionAsync(MonoSink<SearchResult> sink, SearchRequest suggestionSearchRequest, SearchResult searchResult){
        client.searchAsync(
                suggestionSearchRequest
                , DEFAULT
                , ActionListener.wrap(
                        (res)->  {
                            sink.success(addSuggestionResultsToResponse(searchResult, res));
                        }
                        , sink::error));
    }



    private void emitSearchResponse(MonoSink<SearchResult> sink, SearchResponse response){
        sink.success(createSearchResult(response));
    }




    private SearchResult createSearchResult(SearchResponse response) {
        var hits = response.getHits();
        var results = createResults(hits);

        var result = new SearchResult();
        result.setTotal(hits.getTotalHits().value);
        result.setResults(results);
        return result;
    }




    private SearchResult addSuggestionResultsToResponse(SearchResult searchResult, SearchResponse suggestionResponse){
        var suggestions = createSuggestionResult(suggestionResponse);
        var searchResultCpy = new SearchResult();
        copyProperties(searchResult, searchResultCpy);
        searchResultCpy.setSuggestions(suggestions);
        return searchResultCpy;
    }


    private List<String> createSuggestionResult(SearchResponse response) {
        return Arrays
                .stream(response.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .filter(entry -> Objects.equals(entry.getKey(), "name"))
                .map(Map.Entry::getValue)
                .map(Object::toString)
                .distinct()
                .collect(toList());
    }



    private SearchResult.Results createResults(SearchHits hits) {
        var results = new SearchResult.Results();
        results.setTags(filterByType(hits, TAGS));
        results.setCollections(filterByType(hits, COLLECTIONS));
        results.setProducts(filterByType(hits, PRODUCTS));
        return results;
    }




    private List<Map<String, Object>> filterByType(SearchHits hits, SearchType type) {
        return Arrays
                .stream(hits.getHits())
                .filter(hit -> Objects.equals(hit.getIndex(), type.name().toLowerCase()))
                .map(SearchHit::getSourceAsMap)
                .collect(toList());
    }



    private int limitPage(int pageSize){
        return Math.min(pageSize, MAX_PG_SIZE);
    }



    private String getIndex(SearchType type){
        return type.name().toLowerCase();
    }
}



@EqualsAndHashCode(callSuper = true)
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class TagsObject extends TagsRepresentationObject{
    private Long organizationId;
    private Integer yeshteryState;

    TagsObject(TagsRepresentationObject representationObj , OrganizationEntity org){
        this.organizationId = org.getId();
        this.yeshteryState = org.getYeshteryState();
        copyProperties(representationObj, this);
    }
}



@EqualsAndHashCode(callSuper = true)
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Product extends ProductRepresentationObject{
    private List<CsvRow> extraData;
    private Long organizationId;
    private Integer yeshteryState;

    public Product(ProductRepresentationObject repObject, List<CsvRow> extraData, OrganizationEntity org){
        this.extraData = extraData;
        this.organizationId = org.getId();
        this.yeshteryState = org.getYeshteryState();
        copyProperties(repObject, this);
    }
}


class NormalizedSearchParameters extends SearchParameters{
    public boolean only_yeshtery;
}
