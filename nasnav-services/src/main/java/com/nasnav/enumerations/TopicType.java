package com.nasnav.enumerations;

public enum TopicType {
    ORG("ORG"), SHOP("SHOP");

    private String value ;

    TopicType(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
