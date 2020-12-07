package com.nasnav.service;

import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SearchType;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.enumerations.SearchType.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;


@Service
public class SearchServiceImpl implements SearchService{

    private static final int MAX_PG_SIZE = 100;
    private static final int DEFAULT_PG_SIZE = 10;
    private static final String SUGGESTION_NAME = "suggestions";

    @Autowired
    RestHighLevelClient client;


    @Override
    public Mono<SearchResult> search(SearchParameters parameters) {
        SuggestBuilder suggestBuilder = buildSuggestionQuery(parameters);
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(parameters, suggestBuilder);

        //TODO: given types , select indices for search
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);

        return Mono.create(sink -> searchAsync(sink, searchRequest));
    }



    private SearchSourceBuilder getSearchSourceBuilder(SearchParameters parameters, SuggestBuilder suggestBuilder) {
        int from = ofNullable(parameters.start).orElse(0);
        int size = ofNullable(parameters.count).map(this::limitPage).orElse(DEFAULT_PG_SIZE);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(multiMatchQuery(parameters.keyword));
        searchSourceBuilder.suggest(suggestBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        return searchSourceBuilder;
    }



    private SuggestBuilder buildSuggestionQuery(SearchParameters parameters) {
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        PhraseSuggestionBuilder suggestionBuilder = new PhraseSuggestionBuilder("name");
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
                .map(Suggest.Suggestion.Entry::getText)
                .filter(Objects::nonNull)
                .map(Text::string)
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
                .filter(hit -> Objects.equals(hit.getIndex(), type.name()))
                .map(SearchHit::getSourceAsMap)
                .collect(toList());
    }



    private int limitPage(int pageSize){
        return Math.min(pageSize, MAX_PG_SIZE);
    }
}
