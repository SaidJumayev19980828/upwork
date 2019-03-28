package com.nasnav.dao;

import com.nasnav.persistence.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository  extends JpaRepository<Configuration, Long> {

    /**
     * Return Configuration object by key.
     *
     * @param key Key to be used to get configuration.
     * @return Configuration object.
     */
    Configuration getByKey(String key);
}
