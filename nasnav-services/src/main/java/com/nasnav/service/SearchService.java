package com.nasnav.service;

import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import reactor.core.publisher.Mono;

public interface SearchService {
    Mono<SearchResult> search(SearchParameters parameters, boolean onlyYeshtery);

    Mono<Void> syncSearchData();

    Mono<Void> deleteAllIndices();
}
