package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.exceptions.CustomException;
import com.nasnav.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping(value = ServiceController.API_PATH, produces = APPLICATION_JSON_VALUE)
public class ServiceController {

    static final String API_PATH = YeshteryConstants.API_PATH + "/service";

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

}
