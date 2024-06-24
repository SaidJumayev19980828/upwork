package com.nasnav.dao.yeshtery;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Repository
public class CommonYeshteryUserRepositoryImpl implements CommonYeshteryUserRepository{


    @Autowired
    private YeshteryUserRepository userRepo;

    @Override
    public BaseYeshteryUserEntity saveAndFlush(BaseYeshteryUserEntity userEntity) {
       return userRepo.save((YeshteryUserEntity) userEntity);
    }

    @Override
    public BaseYeshteryUserEntity getByEmailAndOrganizationId(String email, Long orgId) {
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
