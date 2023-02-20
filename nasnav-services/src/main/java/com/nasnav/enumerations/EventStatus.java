package com.nasnav.enumerations;

public enum EventStatus {
    PENDING(0), LIVE(1), ENDED(2);

    private Integer value ;

    EventStatus(Integer value){
        this.value = value;
    }
    public Integer getValue(){
        return value;
    }

    public static EventStatus getEnumByValue(int value){
        for(EventStatus e : EventStatus.values()){
            if(e.value.equals(value)) return e;
        }
        return null;
    }
}
