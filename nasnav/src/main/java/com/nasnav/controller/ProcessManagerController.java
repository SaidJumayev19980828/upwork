package com.nasnav.controller;

import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.handler.ImportDataHandlingChainProcessManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class ProcessManagerController {

    private final ImportDataHandlingChainProcessManagerService handlingChainingProcessManagerService;

    @GetMapping
    public List<ImportProcessStatusResponse> getAllProcess(@RequestHeader(name = "User-Token", required = false) String token) {

        return handlingChainingProcessManagerService.getProcessesStatus();
    }

    @GetMapping("{id}/status")
    public ImportProcessStatusResponse getProcessStatus(@RequestHeader(name = "User-Token", required = false) String token,
                                                        @PathVariable String id) {

        return handlingChainingProcessManagerService.getProcessStatus(id);
    }

    @GetMapping("{id}/result")
    public Object getProcessResult(@RequestHeader(name = "User-Token", required = false) String token,
                                   @PathVariable String id) {

        return handlingChainingProcessManagerService.getProcessResult(id);
    }

    @PutMapping("cancel/{id}")
    public ImportProcessStatusResponse cancelProcess(@RequestHeader(name = "User-Token", required = false) String token,
                              @PathVariable String id) {

       return handlingChainingProcessManagerService.cancelProcess(id);
    }

    @DeleteMapping
    public void clearAllProcess(@RequestHeader(name = "User-Token", required = false) String token){
        handlingChainingProcessManagerService.clearAllProcess();
    }

    @DeleteMapping("{id}")
    public void clearProcess(@RequestHeader(name = "User-Token", required = false) String token,
                                @PathVariable String id){
        handlingChainingProcessManagerService.clearProcess(id);
    }

}
