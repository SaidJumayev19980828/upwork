package com.nasnav;

import java.sql.Connection;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;

@Configuration
public class QueryDslConfig {

    @Autowired
    private DataSource dataSource;


    
    
    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration() {
    	com.querydsl.sql.Configuration config = new com.querydsl.sql.Configuration(new PostgreSQLTemplates());
		config.setUseLiterals(true);
		config.setExceptionTranslator(new SpringExceptionTranslator());
        return config;
    }
    

    
    
    @Bean
    public SQLQueryFactory queryFactory() {
        Provider<Connection> provider = new SpringConnectionProvider(dataSource);
        return new SQLQueryFactory(querydslConfiguration(), provider);
    }

}
