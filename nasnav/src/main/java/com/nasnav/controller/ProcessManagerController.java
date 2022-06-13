package com.nasnav.controller;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.service.handler.HandlingChainingProcessManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class ProcessManagerController {

    private final HandlingChainingProcessManagerService handlingChainingProcessManagerService;

    @GetMapping
    public List<HandlerChainProcessStatus> getAllProcess(@RequestHeader(name = "User-Token") String token) {

        return handlingChainingProcessManagerService.getProcessesStatus();
    }

    @GetMapping("{id}/status")
    public HandlerChainProcessStatus getProcessStatus(@RequestHeader(name = "User-Token") String token,
                                                      @PathVariable String id) {

        return handlingChainingProcessManagerService.getProcessStatus(id);
    }

    @GetMapping("{id}/result")
    public Object getProcessResult(@RequestHeader(name = "User-Token") String token,
                                   @PathVariable String id) {

        return handlingChainingProcessManagerService.getProcessResult(id);
    }

    @PutMapping("cancel/{id}")
    public HandlerChainProcessStatus cancelProcess(@RequestHeader(name = "User-Token") String token,
                              @PathVariable String id) {

       return handlingChainingProcessManagerService.cancelProcess(id);
    }

    @DeleteMapping
    public void clearAllProcess(@RequestHeader(name = "User-Token") String token){
        handlingChainingProcessManagerService.clearAllProcess();
    }

    @DeleteMapping("{id}")
    public void clearProcess(@RequestHeader(name = "User-Token") String token,
                                @PathVariable String id){
        handlingChainingProcessManagerService.clearProcess(id);
    }

}
