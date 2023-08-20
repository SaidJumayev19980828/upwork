package com.nasnav.service.impl;

import com.nasnav.dao.EmployeeUserHeartBeatsLogsRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EmployeeUserHeartBeatsLogsEntity;
import com.nasnav.service.EmployeeUserHeartBeatsLogsService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class EmployeeUserHeartBeatsLogsServiceImpl implements EmployeeUserHeartBeatsLogsService {
    private final EmployeeUserHeartBeatsLogsRepository employeeUserHeartBeatsLogsRepository;
    private final SecurityService securityService;

    @Transactional
    @Override
    public void log() {
        BaseUserEntity currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            EmployeeUserHeartBeatsLogsEntity entity = new EmployeeUserHeartBeatsLogsEntity();
            EmployeeUserEntity employeeUser = new EmployeeUserEntity();
            employeeUser.setId(currentUser.getId());
            entity.setEmployeeUserEntity(employeeUser);
            employeeUserHeartBeatsLogsRepository.save(entity);
        }
    }

    @Override
    public List<UserRepresentationObject> getActiveEmployee(Long orgId, Integer threshold, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        LocalDateTime time = LocalDateTime.now().minusMinutes(threshold);
        return employeeUserHeartBeatsLogsRepository.findByOrganizationIdAndUserStatus(orgId, time, page)
                .stream().map(EmployeeUserEntity::getRepresentation).collect(toList());
    }

}
