package com.nasnav.service.impl;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.service.ImportExportHelper;
import com.nasnav.service.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.exceptions.ErrorCodes.P$IMPORT$0002;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class ImportExportHelperImpl implements ImportExportHelper {
    @Autowired
    private SecurityService security;

    @Override
    public void validateAdminCanManageTheShop(Long shopId) {
        if(security.currentUserHasMaxRoleLevelOf(STORE_MANAGER)){
            var user = (EmployeeUserEntity)security.getCurrentUser();
            var userShop = user.getShopId();
            if(!Objects.equals(userShop, shopId)){
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMPORT$0002);
            }
        }
    }
}
