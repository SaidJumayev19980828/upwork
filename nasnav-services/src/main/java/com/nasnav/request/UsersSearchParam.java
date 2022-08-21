package com.nasnav.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class UsersSearchParam extends BaseSearchParams{
    Set<String> roles;
    Long orgId;
    Long shopId;
}
