package com.nasnav.response;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoChatListResponse {
    private Long total;
    private List<VideoChatLogRepresentationObject> sessions;
}
