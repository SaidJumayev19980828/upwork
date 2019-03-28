package com.nasnav.service;

import com.nasnav.dao.ConfigurationRepository;
import com.nasnav.persistence.Configuration;
import com.nasnav.persistence.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nasnav.constatnts.EntityConstants.ConfigurationKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public String getConfigValue(ConfigurationKey key, String defaultValue) {
        Configuration configuration = this.configurationRepository.getByKey(key.getValue());
        String configValue = configuration != null ? configuration.getValue() : null;
        return configValue != null ? configValue : defaultValue;
    }

    @Override
    public Integer getConfigIntValue(ConfigurationKey key, Integer defaultValue) {
        String configValue = getConfigValue(key, null);
        if(configValue == null){
            return defaultValue;
        }
        try {
            return Integer.parseInt(configValue);
        }catch (NumberFormatException ex){
            return defaultValue;
        }
    }


    @Override
    public Map<ConfigurationKey, String> getAllConfig() {
        Map<ConfigurationKey, String> configurationMap = new HashMap<>();
        List<Configuration> configurations = configurationRepository.findAll();
        if(EntityUtils.isBlankOrNull(configurations)){
            return configurationMap;
        }
        for (Configuration configuration : configurations) {
            ConfigurationKey configKey = ConfigurationKey.valueByDbKey(configuration.getKey());
            if (configKey != null ) {
                configurationMap.put(configKey, configuration.getValue());
            }
        }
        return configurationMap;
    }


}
