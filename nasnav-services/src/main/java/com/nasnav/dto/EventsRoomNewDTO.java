package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.sis.util.Static;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventsRoomNewDTO {
    EventsNewDTO eventsNewDTO;
    Boolean canStart;
    String data;
    String sceneId;



    public static EventsRoomNewDTO eventRomBuilder(EventsNewDTO eventsNewDTO, Boolean canStart, String sceneId, String data) {
          return EventsRoomNewDTO.builder()
                  .eventsNewDTO(eventsNewDTO)
                  .canStart(canStart)
                  .sceneId(sceneId)
                  .data(data)
                  .build();
    }
}
