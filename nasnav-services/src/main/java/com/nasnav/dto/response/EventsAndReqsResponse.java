package com.nasnav.dto.response;

import com.nasnav.dto.EventRequestsDTO;
import lombok.Data;

import java.util.List;

@Data
public class EventsAndReqsResponse {
	List<EventResponseDto> events;
	List<EventRequestsDTO> requests;
}
