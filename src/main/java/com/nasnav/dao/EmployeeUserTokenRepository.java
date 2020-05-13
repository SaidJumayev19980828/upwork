package com.nasnav.dao;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserTokensEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EmployeeUserTokenRepository extends CrudRepository<EmployeeUserTokensEntity, Long> {

    EmployeeUserTokensEntity findByToken(String token);

    @Transactional
    @Modifying
    void deleteByToken(String token);

    @Query("select u from EmployeeUserEntity u join EmployeeUserTokensEntity t on t.employeeUserEntity = u where t.token = :token")
    Optional<BaseUserEntity> getEmployeeUserEntityByToken(@Param("token") String token);
}
