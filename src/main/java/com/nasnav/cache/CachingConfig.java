package com.nasnav.cache;

import static java.time.Duration.ofMinutes;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;
import static org.ehcache.config.units.MemoryUnit.MB;

import java.util.List;
import java.util.Map;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;





@Configuration
public class CachingConfig extends CachingConfigurerSupport{
	
	List<String> cachesNames = asList(
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
    		);
 
       
    private static final Long HEAP_SIZE = 100L;
    private static final Long TIME_TO_LIVE = 120L;
    
    
    
     
//    public CacheManager ehCacheManager() {
//        CacheConfiguration<?,?> defaultCacheConfig = 
//        		newCacheConfigurationBuilder(Object.class, Object.class, heap(HEAP_SIZE).offheap(1, MB))
//        		.withExpiry( timeToLiveExpiration(ofMinutes(TIME_TO_LIVE)) )
//        		.build();
//            
//        CacheManagerBuilder<CacheManager> cacheBuilder = newCacheManagerBuilder();
//        
//        cachesNames.forEach( name -> cacheBuilder.withCache(name, defaultCacheConfig));
//        
//        return cacheBuilder.build();
//    }
    
    
    
    
    @Bean
    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        return new JCacheCacheManager(createInMemoryCacheManager());
    }
    
    
    
    
    private CacheManager createInMemoryCacheManager() {

        CacheConfiguration<?,?> defaultCacheConfig = 
        		newCacheConfigurationBuilder(Object.class, Object.class, heap(HEAP_SIZE).offheap(1, MB))
        		.withExpiry( timeToLiveExpiration(ofMinutes(TIME_TO_LIVE)) )
        		.build();

        Map<String, org.ehcache.config.CacheConfiguration<?, ?>> caches = createCacheConfigurations(defaultCacheConfig);

        EhcacheCachingProvider provider = getCachingProvider();
        DefaultConfiguration configuration = new DefaultConfiguration(caches, getClassLoader());
        return getCacheManager(provider, configuration);
    }
    
    
    
    
    private EhcacheCachingProvider getCachingProvider() {
        return (EhcacheCachingProvider) Caching.getCachingProvider();
    }
    
    
    
    private Map<String, org.ehcache.config.CacheConfiguration<?, ?>> createCacheConfigurations(
    		CacheConfiguration<?, ?> cacheConfiguration) {
        return cachesNames
        		.stream()
        		.collect(toMap(name -> name, name -> cacheConfiguration));
    }
    
    
    
    private ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
    
    
    
    
    private CacheManager getCacheManager(EhcacheCachingProvider provider, DefaultConfiguration configuration) {
        return provider.getCacheManager(provider.getDefaultURI(), configuration);
    }
    
}