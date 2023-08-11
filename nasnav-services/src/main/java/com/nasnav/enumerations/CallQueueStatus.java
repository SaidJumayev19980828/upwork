package com.nasnav.enumerations;

public enum CallQueueStatus {
    OPEN(0), LIVE(1), DONE(2), CANCELLED(3), REJECTED(4);

    private Integer value ;

    CallQueueStatus(Integer value){
        this.value = value;
    }
    public Integer getValue(){
        return value;
    }

    public static CallQueueStatus getEnumByValue(int value){
        for(CallQueueStatus e : CallQueueStatus.values()){
            if(e.value.equals(value)) return e;
        }
        return null;
    }
}
