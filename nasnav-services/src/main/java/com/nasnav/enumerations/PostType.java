package com.nasnav.enumerations;

public enum PostType {
    POST(0), REVIEW(1);

    private Integer value ;

    PostType(Integer value){
        this.value = value;
    }
    public Integer getValue(){
        return value;
    }

    public static PostType getEnumByValue(int value){
        for(PostType e : PostType.values()){
            if(e.value.equals(value)) return e;
        }
        return null;
    }
}
