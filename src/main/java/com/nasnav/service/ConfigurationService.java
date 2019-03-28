package com.nasnav.service;

import com.nasnav.constatnts.EntityConstants.ConfigurationKey;

import java.util.Map;

public interface ConfigurationService {

    /**
     * Get configuration value of configuration record from DB by key.
     *
     * @param key configuration key.
     * @param defaultValue default value to be returned in case configuration not found in DB
     * @return configuration value as String
     */
    String getConfigValue(ConfigurationKey key, String defaultValue);

    /**
     * Get configuration value of configuration record from DB by key.
     *
     * @param key configuration key.
     * @param defaultValue default value to be returned in case configuration not found in DB
     * @return configuration value as Integer
     */
    Integer getConfigIntValue(ConfigurationKey key, Integer defaultValue);

    /**
     *
     * @return All Configuration in DB
     */
    Map<ConfigurationKey, String> getAllConfig();
}
