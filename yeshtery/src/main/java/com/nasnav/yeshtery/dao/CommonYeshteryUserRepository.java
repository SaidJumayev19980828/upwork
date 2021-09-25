package com.nasnav.yeshtery.dao;

import com.nasnav.persistence.BaseYeshteryUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommonYeshteryUserRepository {
    List<String> getUserRoles(BaseYeshteryUserEntity user);
    BaseYeshteryUserEntity saveAndFlush(BaseYeshteryUserEntity userEntity) ;
    BaseYeshteryUserEntity getByEmailAndOrganizationId(String email, Long org_id);
    BaseYeshteryUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);
    BaseYeshteryUserEntity findById(Long id, Boolean isEmp);
}
