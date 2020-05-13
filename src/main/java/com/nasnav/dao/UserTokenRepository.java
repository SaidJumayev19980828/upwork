package com.nasnav.dao;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserTokensEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserTokenRepository extends CrudRepository<UserTokensEntity, Long> {

    UserTokensEntity findByToken(String token);

    @Transactional
    @Modifying
    void deleteByToken(String token);

    @Query("select u from UserEntity u join UserTokensEntity t on t.userEntity = u where t.token = :token")
    Optional<BaseUserEntity> getUserEntityByToken(@Param("token") String token);
}
