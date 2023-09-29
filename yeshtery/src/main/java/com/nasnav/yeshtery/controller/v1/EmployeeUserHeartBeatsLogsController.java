package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.service.EmployeeUserHeartBeatsLogsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(EmployeeUserHeartBeatsLogsController.API_PATH)
@AllArgsConstructor
public class EmployeeUserHeartBeatsLogsController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/employee-user-heart-beats-logs";
    private final EmployeeUserHeartBeatsLogsService employeeUserHeartBeatsLogsService;

    @PostMapping(value = "/log")
    public ResponseEntity<Void> log(@RequestHeader(name = "User-Token", required = false) String userToken) {
        employeeUserHeartBeatsLogsService.log();
        return ResponseEntity.noContent().build();
    }


    @GetMapping(value = "list-active-employee", produces = APPLICATION_JSON_VALUE)
    public List<UserRepresentationObject> getActiveEmployee(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                            @RequestParam(value = "org_id") Long orgId,
                                                            @RequestParam(value = "threshold", required = false, defaultValue = "5") Integer threshold,
                                                            @RequestParam(required = false, defaultValue = "0") Integer start,
                                                            @RequestParam(required = false, defaultValue = "10") Integer count) {
        return employeeUserHeartBeatsLogsService.getActiveEmployee(orgId, threshold, start, count);
    }

}
