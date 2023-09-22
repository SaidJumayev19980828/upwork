package com.nasnav.controller;

import com.nasnav.dto.request.PackageDto;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/package")
@CrossOrigin("*") // allow all origins
@RequiredArgsConstructor
public class PackageController {
    private final PackageService packageService;

    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public List<PackageResponse> getListPackage() {
        return packageService.getPackages();
    }

    @PostMapping(value = "create", produces = APPLICATION_JSON_VALUE)
    public void createPackage(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody PackageDto packageDto) throws Exception {
        packageService.createPackage(packageDto);
    }

    @PutMapping("/{packageId}")
    public void updatePackage(@RequestHeader(name = "User-Token", required = false) String token, @RequestBody PackageDto packageDto, @PathVariable Long packageId){
        packageService.updatePackage(packageDto, packageId);
    }

    @DeleteMapping(value = "{packageId}")
    public void removePackage(@RequestHeader(name = "User-Token", required = false) String token,@PathVariable Long packageId) {
        packageService.removePackage(packageId);
    }

    @PostMapping(value = "register-package-profile", produces = APPLICATION_JSON_VALUE)
    public Long registerPackageProfile(@RequestHeader(name = "User-Token", required = false) String token,
                                       @Valid @RequestBody PackageRegisteredByUserDTO packageRegisteredByUserDTO) throws Exception {
        return packageService.registerPackageProfile(packageRegisteredByUserDTO);
    }
}
