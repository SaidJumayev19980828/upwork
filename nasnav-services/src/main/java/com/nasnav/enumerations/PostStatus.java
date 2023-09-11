package com.nasnav.enumerations;

public enum PostStatus {
    PENDING(0), APPROVED(1), REJECTED(2);

    private Integer value ;

    PostStatus(Integer value){
        this.value = value;
    }
    public Integer getValue(){
        return value;
    }

    public static PostStatus getEnumByValue(int value){
        for(PostStatus e : PostStatus.values()){
            if(e.value.equals(value)) return e;
        }
        return null;
    }
}
