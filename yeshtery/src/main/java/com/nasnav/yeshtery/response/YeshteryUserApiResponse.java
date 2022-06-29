package com.nasnav.yeshtery.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.servlet.http.Cookie;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
public class YeshteryUserApiResponse extends UserApiResponse {

    public YeshteryUserApiResponse(Long entityId, String token, List<String> roles, Long organizationId, Long storeId,
                           String name, String email, Cookie cookie) {
        super(entityId, token, roles, organizationId, storeId, name, email, cookie);
    }

    public YeshteryUserApiResponse(Long entityId, List<ResponseStatus> statuses) {
        super(entityId, statuses);
    }

    public YeshteryUserApiResponse(Long entityId, String orgUrl, String token) {
        super(entityId, orgUrl, token);
    }
}
