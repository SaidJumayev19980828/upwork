package com.nasnav.service;

import com.nasnav.commons.utils.EntityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SearchType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.Roles.ORGANIZATION_ADMIN;
import static com.nasnav.enumerations.SearchType.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.action.ActionListener.wrap;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.common.xcontent.XContentType.JSON;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service
public class SearchServiceImpl implements SearchService{

    private final static Logger logger = LogManager.getLogger();

    private static final int MAX_PG_SIZE = 100;
    private static final int DEFAULT_PG_SIZE = 10;
    private static final String SUGGESTION_NAME = "suggestions";

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


    @Override
    public Mono<SearchResult> search(SearchParameters parameters) {
        if(anyIsNull(parameters, parameters.org_id, parameters.keyword)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, NAVBOX$SRCH$0001);
        }

        SuggestBuilder suggestBuilder = buildSuggestionQuery(parameters);
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(parameters, suggestBuilder);

        String[] indices = getIndices(parameters);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        if(indices.length > 0){
            searchRequest.indices(indices);
        }

        return Mono
                .<SearchResult>create(sink -> searchAsync(sink, searchRequest))
                .doOnError(e -> logger.error(e,e));
    }



    @Override
    public Mono<Void> syncSearchData() {
        List<Long> organizationsToSync = getOrgsToSync();

        return Flux
                .fromIterable(organizationsToSync)
                .flatMap(this::doSyncSearchData)
                .reduce((res1, res2) -> {return res2;});
    }



    private List<Long> getOrgsToSync() {
        if(securityService.currentUserHasRole(NASNAV_ADMIN)){
            return orgRepo.findAllOrganizations();
        }
        else if(securityService.currentUserHasRole(ORGANIZATION_ADMIN)){
            Long orgId = securityService.getCurrentUserOrganizationId();
            return singletonList(orgId);
        }else{
            return emptyList();
        }
    }



    private Mono<Void> doSyncSearchData(Long orgId){
        return deleteOrganizationData(orgId)
                .then(resendOrganizationData(orgId));
    }



    private Mono<Void> deleteOrganizationData(Long orgId){
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
        DeleteByQueryRequest request = new DeleteByQueryRequest(name);
        request.setQuery(new TermQueryBuilder("organization_id", orgId));
        return Mono
                .<Void>create(sink -> client.deleteByQueryAsync(request, DEFAULT
                        , wrap((res)-> sink.success(), sink::error)))
                .doOnError(e -> logger.error(e,e));
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




    private Mono<Void> resendOrganizationData(Long orgId){
        return Mono
                .zip(sendProductsAndCollectionsData(orgId), sendTagsData(orgId))
                .then();
    }



    private Mono<Void> sendTagsData(Long orgId) {
        BulkRequest request = new BulkRequest();
        categoryService
                .getOrganizationTags(orgId, null)
                .stream()
                .map(tag -> new TagsObject(tag, orgId))
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
            String errMsg = response.buildFailureMessage();
            Throwable e = new RuntimeBusinessException(NOT_ACCEPTABLE, SRCH$SYNC$0002, orgId, errMsg);
            sink.error(e);
        }

    }



    private <T> Optional<IndexRequest> createIndexRequest(SearchType type ,T obj){
        String index = getIndex(type);
        IndexRequest request = new IndexRequest(index);
        try {
            String json = objectMapper.writeValueAsString(obj);
            request.source(json, JSON);
            return Optional.of(request);
        } catch (JsonProcessingException e) {
            logger.error(e,e);
            return empty();
        }
    }




    private Mono<Void> sendProductsAndCollectionsData(Long orgId) {
        BulkRequest request = new BulkRequest();
        Map<Long,List<CsvRow>> extraData = getProductsExtraData(orgId);
        Long totalProducts = getTotalProducts(orgId);
        double batchSize = 1000;
        int batches = (int)Math.ceil(totalProducts/batchSize);
        logger.info(format("Sync search data for org[%d]: sync [%d] product and collections in [%d] batches!", orgId, totalProducts, batches));
        for(int i=0; i< batches; i++ ){
            try {
                List<Product> products = getProductsBatch(orgId, extraData, batchSize, i);
                addProductsToBulkIndexRequest(request, products);
                addCollectionsToBulkIndexRequest(request, products);
            } catch (Throwable e) {
                logger.error(e,e);
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, SRCH$SYNC$0001, orgId);
            }
        }
        return Mono
                .<Void>create(sink -> client.bulkAsync(request, DEFAULT, wrap((res)-> handleBulkResponse(res, sink, orgId), sink::error)))
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



    private List<Product> getProductsBatch(Long orgId, Map<Long, List<CsvRow>> extraData, double batchSize, int i) throws BusinessException, InvocationTargetException, IllegalAccessException {
        int start = (int)(batchSize * i);
        ProductSearchParam params = new ProductSearchParam();
        params.org_id = orgId;
        params.count = (int) batchSize;
        params.start = start;
        return productService
                .getProducts(params)
                .getProducts()
                .stream()
                .map(prod -> new Product(prod, extraData.get(prod.getId()), orgId))
                .collect(toList());
    }



    private Long getTotalProducts(Long orgId) {
        ProductSearchParam params = new ProductSearchParam();
        params.org_id = orgId;
        params.count = 10;
        params.start = 0;
        try {
            return  productService.getProducts(params).getTotal();
        } catch (BusinessException|InvocationTargetException|IllegalAccessException e) {
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



    private SearchSourceBuilder getSearchSourceBuilder(SearchParameters parameters, SuggestBuilder suggestBuilder) {
        int from = ofNullable(parameters.start).orElse(0);
        int size = ofNullable(parameters.count).map(this::limitPage).orElse(DEFAULT_PG_SIZE);

        return new SearchSourceBuilder()
                .query( QueryBuilders
                        .boolQuery()
                        .must(
                            multiMatchQuery(parameters.keyword)
                                .fuzziness(Fuzziness.AUTO)
                                .field("name", 3) //give priority to field "name"
                                .field("*"))
                        .filter(matchQuery("organization_id", parameters.org_id)))
                .suggest(suggestBuilder)
                .from(from)
                .size(size);
    }



    private SuggestBuilder buildSuggestionQuery(SearchParameters parameters) {
        DirectCandidateGeneratorBuilder generatorBuilder = new DirectCandidateGeneratorBuilder("name");
        generatorBuilder.suggestMode("always");

        PhraseSuggestionBuilder suggestionBuilder = new PhraseSuggestionBuilder("name");
        suggestionBuilder.addCandidateGenerator(generatorBuilder);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(SUGGESTION_NAME, suggestionBuilder);
        suggestBuilder.setGlobalText(parameters.keyword);
        return suggestBuilder;
    }



    private void searchAsync(MonoSink<SearchResult> sink, SearchRequest searchRequest ){
        client.searchAsync(
                searchRequest
                , DEFAULT
                , ActionListener.wrap((res)-> emitResponse(sink, res), sink::error));
    }



    private void emitResponse(MonoSink<SearchResult> sink, SearchResponse response){
        sink.success(createSearchResult(response));
    }



    private SearchResult createSearchResult(SearchResponse response) {
        SearchHits hits = response.getHits();
        SearchResult.Results results = createResults(hits);
        List<String> suggestions =  getSuggestions(response);

        SearchResult result = new SearchResult();
        result.setTotal(hits.getTotalHits().value);
        result.setSuggestions(suggestions);
        result.setResults(results);
        return result;
    }




    private List<String> getSuggestions(SearchResponse response) {
        return of(response)
                .map(SearchResponse::getSuggest)
                .map(suggest -> suggest.<PhraseSuggestion>getSuggestion(SUGGESTION_NAME))
                .map(Suggest.Suggestion::getEntries)
                .orElse(emptyList())
                .stream()
                .map(Suggest.Suggestion.Entry::getOptions)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(Suggest.Suggestion.Entry.Option::getText)
                .map(Text::toString)
                .collect(toList());
    }


    private SearchResult.Results createResults(SearchHits hits) {
        SearchResult.Results results = new SearchResult.Results();
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

    TagsObject(TagsRepresentationObject representationObj , Long orgId){
        this.organizationId = orgId;
        copyProperties(representationObj, this);
    }
}



@EqualsAndHashCode(callSuper = true)
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Product extends ProductRepresentationObject{
    private List<CsvRow> extraData;
    private Long organizationId;

    public Product(ProductRepresentationObject repObject, List<CsvRow> extraData, Long orgId){
        this.extraData = extraData;
        this.organizationId = orgId;
        copyProperties(repObject, this);
    }
}
