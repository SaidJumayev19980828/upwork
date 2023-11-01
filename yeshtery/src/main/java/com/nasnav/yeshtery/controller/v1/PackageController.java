package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.request.PackageDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = PackageController.API_PATH, produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*") // allow all origins
@RequiredArgsConstructor
public class PackageController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/package";

    private final PackageService packageService;

    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public List<PackageResponse> getListPackage() {
        return packageService.getPackages();
    }

    @PostMapping(value = "create", produces = APPLICATION_JSON_VALUE)
    public PackageResponse createPackage(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody PackageDTO packageDto) throws Exception {
        return packageService.createPackage(packageDto);
    }

    @PutMapping("/{packageId}")
    public PackageResponse updatePackage(@RequestHeader(name = "User-Token", required = false) String token, @RequestBody PackageDTO packageDto, @PathVariable Long packageId){
        return packageService.updatePackage(packageDto, packageId);
    }

    @DeleteMapping(value = "{packageId}")
    public void removePackage(@RequestHeader(name = "User-Token", required = false) String token,@PathVariable Long packageId) {
        packageService.removePackage(packageId);
    }

    @PostMapping(value = "register-package-profile", produces = APPLICATION_JSON_VALUE)
    public void registerPackageProfile(@RequestHeader(name = "User-Token", required = false) String token,
                                       @Valid @RequestBody PackageRegisteredByUserDTO packageRegisteredByUserDTO) throws Exception {
        packageService.registerPackageProfile(packageRegisteredByUserDTO);
    }
}
