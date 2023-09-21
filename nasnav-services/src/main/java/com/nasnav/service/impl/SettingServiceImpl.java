package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.jalokim.propertiestojson.util.PropertiesToJsonConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Service
@Slf4j
public class SettingServiceImpl implements SettingService {
    @Autowired
    @Qualifier("frontendProps")
    private Properties frontEndProps;

    @Autowired
    private ObjectMapper mapper;
    @Override
    public Map<String, Object> frontEndSettings(String key) {
        final var propJson = getKeyObjectAsJson(key);
        return getKeyValueAsMap(key, propJson);
    }

    private Map<String, Object> getKeyValueAsMap(String key, String propJson) {
        HashMap<String, Object> res = new HashMap<>();
        try {
            Map<Object, Object> propMap = mapper.readValue(propJson, new TypeReference<>() {
            });
            Map<?, ?> objectMap = (Map<?, ?>) propMap.get(key);
            objectMap.forEach((key1, value) -> res.put(((String) key1).toUpperCase(), value));
        } catch (JsonProcessingException e) {
            throw new RuntimeBusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.FRT$VARS001);
        } catch (NullPointerException ex) {
            /*
                in case of wrong key or empty file
                 should we return an empty response (Current Impl)!
                 or
                 throw an exception !
             */
            log.info("Empty File Or Wrong Key ");
        }
        return res;
    }

    private String getKeyObjectAsJson(String key) {
        final var props = new Properties();
        frontEndProps.entrySet()
                .stream()
                .filter(entry->entry.getKey() instanceof String && ((String) entry.getKey()).startsWith(key.concat(".")))
                .forEach(matchedEntry -> props.put(matchedEntry.getKey(),matchedEntry.getValue()));
        return new PropertiesToJsonConverter().convertToJson(props);
    }
}
