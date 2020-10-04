package com.nasnav.commons.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


/**
 * A solution for reading some json fields as raw json string using jackson.
 * this was copied from
 * https://dolzhenko.me/blog/2017-08-13-raw-jackson
 * */
public class RawObjectSerializer extends StdSerializer<RawObject> {

    public RawObjectSerializer() {
        super(RawObject.class);
    }

    @Override
    public void serialize(RawObject value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        if (value.getValue() == null) {
            generator.writeNull();
        } else {
            generator.writeRawValue(value.getValue());
        }
    }
}
