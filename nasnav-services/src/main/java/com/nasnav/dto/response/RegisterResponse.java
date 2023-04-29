package com.nasnav.dto.response;

import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.UserApiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private OrganizationResponse organizationResponse ;

    private UserApiResponse userApiResponse ;
}
