package com.nasnav.enumerations;

public enum EventRequestStatus {

    PENDING(0), APPROVED(1), REJECTED(2);

    private Integer value ;

    EventRequestStatus(Integer value){
        this.value = value;
    }
    public Integer getValue(){
        return value;
    }

    public static EventRequestStatus getEnumByValue(int value){
        for(EventRequestStatus e : EventRequestStatus.values()){
            if(e.value.equals(value)) return e;
        }
        return null;
    }
}
