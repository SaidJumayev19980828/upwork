package com.nasnav.enumerations;

import java.time.LocalDateTime;

public enum EventStatus {
    PENDING(0), LIVE(1), ENDED(2), RUNNING(3), UPCOMING(4), FINISHED(5);

    private Integer value ;

    EventStatus(Integer value){
        this.value = value;
    }

    public static EventStatus getStatusRepresentation(LocalDateTime startsAt, LocalDateTime endsAt) {
        LocalDateTime now = LocalDateTime.now();
        if (startsAt == null || endsAt == null) return PENDING;
        else if (now.isAfter(startsAt) && now.isBefore(endsAt)) {
            return LIVE;
        } else if (now.isAfter(startsAt) && now.isAfter(endsAt)) {
            return ENDED;
        } else {
            return null;
        }
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
