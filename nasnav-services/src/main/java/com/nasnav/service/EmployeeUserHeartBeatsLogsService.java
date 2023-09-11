package com.nasnav.service;

import com.nasnav.dto.UserRepresentationObject;

import java.util.List;

public interface EmployeeUserHeartBeatsLogsService {
    void log();

    List<UserRepresentationObject> getActiveEmployee(Long orgId, Integer threshold, Integer start, Integer count);
}
