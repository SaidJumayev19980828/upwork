package com.nasnav.dao.yeshtery;

import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;
import org.springframework.stereotype.Repository;


@Repository
public interface CommonYeshteryUserRepository {
    BaseYeshteryUserEntity saveAndFlush(BaseYeshteryUserEntity userEntity);

    BaseYeshteryUserEntity getByEmailAndOrganizationId(String email, Long orgId);

    BaseYeshteryUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);

    BaseYeshteryUserEntity findById(Long id, Boolean isEmp);
}
