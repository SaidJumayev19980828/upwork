package com.nasnav.dao;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserTokensEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


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


    @Query(value = "select t from UserTokensEntity t where t.employeeUserEntity = :emp")
	Set<UserTokensEntity> getByEmployeeUserEntity(@Param("emp") EmployeeUserEntity emp);

    @Query(value = "select t from UserTokensEntity t where t.employeeUserEntity in :employees")
    Set<UserTokensEntity> getByEmployeeUserEntities(@Param("employees") Set<EmployeeUserEntity> employees);

    @Transactional
    @Modifying
    @Query(value = "delete from UserTokensEntity t where t.employeeUserEntity = :emp")
	void deleteByEmployeeUserEntity(@Param("emp") EmployeeUserEntity emp);

    @Query(value = "select t from UserTokensEntity t where t.userEntity = :user")
	Set<UserTokensEntity> getByUserEntity(@Param("user") UserEntity user);

    @Query(value = "select t from UserTokensEntity t where t.userEntity in :users")
	Set<UserTokensEntity> getByUserEntities(@Param("users") Set<UserEntity> user);

    @Transactional
    @Modifying
    @Query(value = "delete from UserTokensEntity t where t.userEntity = :usr")
    void deleteByUserEntity(@Param("usr") UserEntity usr);

    @Query("select e.organizationId from UserTokensEntity t inner join t.employeeUserEntity e where t.token = :token")
    Long findEmployeeOrgIdByToken(@Param("token") String token);
}
