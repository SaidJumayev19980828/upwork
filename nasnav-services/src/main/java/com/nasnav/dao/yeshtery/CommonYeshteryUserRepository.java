package com.nasnav.dao.yeshtery;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonYeshteryUserRepository {
    List<String> getUserRoles(BaseUserEntity user);
    List<String> getUserRoles(BaseYeshteryUserEntity user);
    BaseYeshteryUserEntity saveAndFlush(BaseYeshteryUserEntity userEntity) ;
    BaseYeshteryUserEntity getByEmailAndOrganizationId(String email, Long org_id);
    BaseYeshteryUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);
    BaseYeshteryUserEntity findById(Long id, Boolean isEmp);
}
