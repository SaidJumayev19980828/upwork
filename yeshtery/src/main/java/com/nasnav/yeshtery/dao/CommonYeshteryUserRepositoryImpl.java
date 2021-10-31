package com.nasnav.yeshtery.dao;

import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Repository
public class CommonYeshteryUserRepositoryImpl implements CommonYeshteryUserRepository{


    @Autowired
    private YeshteryUserRepository userRepo;



    @Override
    public List<String> getUserRoles(BaseYeshteryUserEntity user) {
        if(user == null)
            return new ArrayList<>();
       return getCustomerUserRoles();
    }

    @Override
    public List<String> getUserRoles(BaseUserEntity user) {
        if(user == null)
            return new ArrayList<>();
        return getCustomerUserRoles();
    }

    private List<String> getCustomerUserRoles() {
        // for now, return default role which is Customer
        return Collections.singletonList(Roles.CUSTOMER.name());
    }

    @Override
    public BaseYeshteryUserEntity saveAndFlush(BaseYeshteryUserEntity userEntity) {
       return userRepo.save((YeshteryUserEntity) userEntity);
    }

    @Override
    public BaseYeshteryUserEntity getByEmailAndOrganizationId(String email, Long org_id) {
       return userRepo.getByEmail(email);
    }

    @Override
    public BaseYeshteryUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee) {
        if(isEmployee != null && isEmployee) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0004);
        }else {
            if (isBlankOrNull(orgId)) {
                throw new RuntimeBusinessException(UNAUTHORIZED, U$LOG$0002);
            }
            return userRepo.getByEmailIgnoreCase(email);
        }
    }

    @Override
    public BaseYeshteryUserEntity findById(Long id, Boolean isEmp) {
        if(isEmp) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0004);
        }else {
            Optional<BaseYeshteryUserEntity> user = userRepo.findById(id)
                    .map(BaseYeshteryUserEntity.class::cast);
            if(user.isPresent())
                return user.get();
            throw  new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, id);
        }
    }
}
