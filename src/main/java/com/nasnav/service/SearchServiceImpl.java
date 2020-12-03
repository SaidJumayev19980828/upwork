package com.nasnav.service;

import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class SearchServiceImpl implements SearchService{

    RestHighLevelClient client;


    @Override
    public Mono<SearchResult> search(SearchParameters parameters) {
        return null;
    }
}
