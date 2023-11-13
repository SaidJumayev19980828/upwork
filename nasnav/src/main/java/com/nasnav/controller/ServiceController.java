package com.nasnav.controller;

import com.nasnav.dto.OrganizationServicesDto;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.exceptions.CustomException;
import com.nasnav.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/service")
public class ServiceController {

    private final ServiceInterface serviceImpl;
    @PostMapping()
    public ServiceResponse createService(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @RequestBody ServiceDTO service){
        log.info("Create Service request received: {}", service);
        try{
            return serviceImpl.createService(service);
        }catch (CustomException e){
            log.error("Error while processing Create Service", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Create Service", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ServiceResponse updateService(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @PathVariable(name = "id") Long id,
                                         @RequestBody ServiceDTO service){
        log.info("Update Service request received: {}", service);
        try{
            return serviceImpl.updateService(id, service);
        }catch (CustomException e){
            log.error("Error while processing Update Service", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Update Service", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteService(@RequestHeader(name = "User-Token", required = false) String userToken,
                              @PathVariable(name = "id") Long id){
        log.info("Delete Service request received: {}", id);
        try{
             serviceImpl.deleteService(id);
        }catch (CustomException e){
            log.error("Error while processing Delete Service", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Delete Service", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ServiceResponse getService(@RequestHeader(name = "User-Token", required = false) String userToken,
                                      @PathVariable(name = "id") Long id){
        log.info("Get Service request received: {}", id);
        try{
            return serviceImpl.getService(id);
        }catch (CustomException e){
            log.error("Error while processing Get Service", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Get Service", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping()
    public List<ServiceResponse> getService(@RequestHeader(name = "User-Token", required = false) String userToken){
        log.info("Get All Services request received ." );
        try{
            return serviceImpl.getALlServices();
        }catch (CustomException e){
            log.error("Error while processing Get All Services", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Get All Services", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/org")
    public List<OrganizationServicesDto> getOrgServices(@RequestHeader(name = "User-Token", required = false) String userToken){
        log.info("Get Organization Services request received ." );
        try{
            return serviceImpl.getOrgServices();
        }catch (CustomException e){
            log.error("Error while processing Get Organization Services", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Get Organization Services", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/org")
    public List<OrganizationServicesDto> updateOrgServices(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                           @RequestBody List<OrganizationServicesDto> request){
        log.info("Update Organization Services request received: {}", request);
        try{
            return serviceImpl.updateOrgService(request);
        }catch (CustomException e){
            log.error("Error while processing Update Organization Services", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Update Organization Services", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
