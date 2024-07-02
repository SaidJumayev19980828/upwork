package com.nasnav.dto.response;

import com.nasnav.enumerations.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderUserResponse {
    private long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private int userStatus;
    private String image;
    private LocalDateTime creationTime;
}
