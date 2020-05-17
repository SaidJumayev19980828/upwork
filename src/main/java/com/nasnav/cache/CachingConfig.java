package com.nasnav.cache;

import static com.nasnav.cache.Caches.BRANDS;
import static com.nasnav.cache.Caches.FILES;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;
import static com.nasnav.cache.Caches.ORGANIZATIONS_CATEGORIES;
import static com.nasnav.cache.Caches.ORGANIZATIONS_DOMAINS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_EXTRA_ATTRIBUTES;
import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_TAGS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_TAG_TREES;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;
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
			FILES
    		, BRANDS
    		, ORGANIZATIONS_BY_NAME
    		, ORGANIZATIONS_BY_ID
    		, ORGANIZATIONS_DOMAINS
    		, ORGANIZATIONS_SHOPS
    		, ORGANIZATIONS_EXTRA_ATTRIBUTES
    		, ORGANIZATIONS_CATEGORIES
    		, ORGANIZATIONS_TAG_TREES
    		, ORGANIZATIONS_TAGS
    		, SHOPS_BY_ID
    		);
 
       
    private static final Long HEAP_SIZE = 16384L;
    private static final Long TIME_TO_LIVE_MIN = 30L;
    public static final int MAX_CACHED_OBJECT_SIZE_MB = 4;
    public static final int MAX_OFF_HEAP_SIZE_MB = 512;
    
    
    
    
    
    @Bean
    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        return new JCacheCacheManager(createInMemoryCacheManager());
    }
    
    
    
    
    private CacheManager createInMemoryCacheManager() {

        CacheConfiguration<?,?> defaultCacheConfig = 
        		newCacheConfigurationBuilder(Object.class, Object.class, heap(HEAP_SIZE).offheap(MAX_OFF_HEAP_SIZE_MB, MB))
        		.withValueSerializer(KryoSerializer.class)
        		.withExpiry( timeToLiveExpiration(ofMinutes(TIME_TO_LIVE_MIN)) )  
        		.withSizeOfMaxObjectSize(MAX_CACHED_OBJECT_SIZE_MB, MB)
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