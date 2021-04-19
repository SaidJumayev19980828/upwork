package com.nasnav.commons.json.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * A solution for reading some json fields as raw json string using jackson.
 * this was copied from
 * https://dolzhenko.me/blog/2017-08-13-raw-jackson
 * */
@JsonSerialize(using = RawObjectSerializer.class)
@JsonDeserialize(using = RawObjectDeserializer.class)
public class RawObject {

    public final String value;

    public RawObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
