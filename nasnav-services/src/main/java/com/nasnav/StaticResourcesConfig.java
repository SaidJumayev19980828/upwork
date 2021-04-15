package com.nasnav;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.concurrent.TimeUnit;

import static com.nasnav.constatnts.ConfigConstants.STATIC_FILES_URL;
import static java.util.Optional.ofNullable;
import static org.springframework.http.CacheControl.maxAge;



@Configuration
public class StaticResourcesConfig implements WebMvcConfigurer {
	@Autowired
	private AppConfig appConfig;

	
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	String staticFilesLocation = getStaticFilesLocationPath();
        registry
        .addResourceHandler(STATIC_FILES_URL+"/**")
        .addResourceLocations("file:" + staticFilesLocation)
        .setCacheControl(maxAge(365, TimeUnit.DAYS))
        .resourceChain(true)
        .addResolver(new PathResourceResolver());
    }


	private String getStaticFilesLocationPath() {
		return ofNullable(appConfig.getBasePathStr())
				.map(path -> path.endsWith("/")? path: path + "/")
				.orElse("./");
	}
}
