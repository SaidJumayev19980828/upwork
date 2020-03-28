package com.nasnav.cache;

import static java.util.Arrays.asList;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

@Component
public class CachingConfig 
  implements CacheManagerCustomizer<ConcurrentMapCacheManager> {
	
 
    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        cacheManager
        .setCacheNames(asList(
        		"files"
        		, "brands"
        		, "organizations_by_name"
        		, "organizations_by_id"
        		, "organizations_domains"
        		, "organizations_shops"
        		, "organizations_extra_attributes"
        		, "organizations_categories"
        		, "organizations_tag_trees"
        		, "organizations_tags"
        		, "shops_by_id"
        		));
    }
    
}