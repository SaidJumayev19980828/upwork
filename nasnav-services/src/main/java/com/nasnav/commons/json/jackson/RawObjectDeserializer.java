package com.nasnav.commons.json.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Objects;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;


/**
 * A solution for reading some json fields as raw json string using jackson.
 * this was copied from
 * https://dolzhenko.me/blog/2017-08-13-raw-jackson
 * */
public class RawObjectDeserializer extends StdDeserializer<RawObject> {

    public RawObjectDeserializer() {
        super(RawObject.class);
    }

    @Override
    public RawObject deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        TreeNode treeNode = parser.getCodec().readTree(parser);
        String value = treeNode.toString();
        if(Objects.equals(treeNode.asToken(), VALUE_STRING)
            && value.length() >= 2){
            value = value.substring(1,value.length()-1);
        }
        return new RawObject(value);
    }
}