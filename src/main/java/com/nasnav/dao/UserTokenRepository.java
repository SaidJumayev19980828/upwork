package com.nasnav.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.UserTokensEntity;

public interface UserTokenRepository extends CrudRepository<UserTokensEntity, Long> {

    UserTokensEntity findByToken(String token);

    @Transactional
    @Modifying
    void deleteByToken(String token);

    @Query("select t from UserTokensEntity t left join fetch t.userEntity left join fetch t.employeeUserEntity" +
            " where t.token = :token")
    UserTokensEntity getUserEntityByToken(@Param("token") String token);

	boolean existsByToken(String token);

	long countByUserEntity_Id(Long userId);

	UserTokensEntity saveAndFlush(UserTokensEntity token);

}
