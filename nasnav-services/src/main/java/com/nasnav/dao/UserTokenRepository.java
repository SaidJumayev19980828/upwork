package com.nasnav.dao;

import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserTokensEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserTokenRepository extends CrudRepository<UserTokensEntity, Long> {

    UserTokensEntity findByToken(String token);

    @Transactional
    @Modifying
    void deleteByToken(String token);

    @Query("select t from UserTokensEntity t left join fetch t.userEntity left join fetch t.employeeUserEntity" +
            " where t.token = :token")
    UserTokensEntity getUserEntityByToken(@Param("token") String token);

    @Query("select count( distinct t.userEntity) from UserTokensEntity t " +
            " where t.userEntity.organizationId = :orgId and t.updateTime >= :startDate")
    Long countActiveUsers(@Param("orgId") Long orgId, @Param("startDate") LocalDateTime startDate);

	boolean existsByToken(String token);

	long countByUserEntity_Id(Long userId);

    long countByEmployeeUserEntity_Id(Long employeeUserId);

	UserTokensEntity saveAndFlush(UserTokensEntity token);

    @Transactional
    @Modifying
    @Query(value = "delete from UserTokensEntity t where t.employeeUserEntity = :emp")
	void deleteByEmployeeUserEntity(@Param("emp") EmployeeUserEntity emp);


    @Transactional
    @Modifying
    @Query(value = "delete from UserTokensEntity t where t.userEntity = :usr")
    void deleteByUserEntity(@Param("usr") UserEntity usr);

    @Query("select e.organizationId from UserTokensEntity t inner join t.employeeUserEntity e where t.token = :token")
    Long findEmployeeOrgIdByToken(@Param("token") String token);
}
