package com.nasnav.cache;

import com.nasnav.service.SecurityService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import java.lang.annotation.Annotation;


@Component
public class CurrentOrgCacheKeyGenerator implements CacheKeyGenerator{

	
	@Autowired
	SecurityService security;
	
	
	@Override
	public GeneratedCacheKey generateCacheKey(
			CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
		Long orgId = security.getCurrentUserOrganizationId();
		return new SimpleGeneratedCacheKey(orgId);
	}

}




class SimpleGeneratedCacheKey extends SimpleKey implements GeneratedCacheKey{
	private static final long serialVersionUID = 1L;

	public SimpleGeneratedCacheKey(Object... params) {
		super(params);
	}
}


@Data
class LongGeneratedCacheKey implements GeneratedCacheKey{
	private static final long serialVersionUID = 1L;
	private Long value;
	
	public LongGeneratedCacheKey(Long value) {
		this.value = value;
	}
}




@Data
class ArrayGeneratedCacheKey implements GeneratedCacheKey{
	private static final long serialVersionUID = 1L;
	private Object[] value;
	
	public ArrayGeneratedCacheKey(Object... value) {
		this.value = value;
	}
}