package com.nasnav.test.external.json.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.json.jackson.RawObject;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
public class RawObjectTest  extends AbstractTestWithTempBaseDir {

    @Autowired
    private ObjectMapper mapper;



    public static class Message {
        public RawObject data;
        public String field;
    }



    @Test
    public void deserialization() throws Exception {
        String fieldValue = "value";
        String jsonValue = "{\"value\":{\"text\":\"123\"}}";
        String jsonMessage = "{\"data\":" + jsonValue + ",\"field\":\"" + fieldValue + "\"}";

        Message message = mapper.readValue(jsonMessage, Message.class);

        Assert.assertEquals(jsonValue, message.data.getValue());
        Assert.assertEquals(fieldValue, message.field);
    }



    @Test
    public void serialization() throws Exception {
        String fieldValue = "value";
        String jsonValue = "{\"value\":{\"text\":\"123\"}}";
        String jsonMessage = "{\"data\":" + jsonValue + ",\"field\":\"" + fieldValue + "\"}";

        Message message = new Message();
        message.data = new RawObject(jsonValue);
        message.field = fieldValue;

        Assert.assertEquals(jsonMessage, mapper.writeValueAsString(message));
    }
}
