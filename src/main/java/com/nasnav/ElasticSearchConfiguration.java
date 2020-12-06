package com.nasnav;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * couldn't use spring-data-elastic search.
 * we used elastic search 7.10, which requires spring-data-elasticsearch 4.x, but this is not
 * supported with spring boot 2.1.x, and when tried using it, it seems there where missing classes in
 * spring-data-common.
 * - so for now we are using the RestHighLevelClient until we upgrade spring boot to 2.3.x
 * */
@Configuration
public class ElasticSearchConfiguration {

    @Value("${nasnav.elasticsearch.url}")
    private String hostAndPort;

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostAndPort)));
    }
}