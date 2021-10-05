package com.nasnav.yeshtery.dao;

import com.nasnav.persistence.YeshteryUserEntity;
import com.nasnav.persistence.YeshteryUserTokensEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface YeshteryUserTokenRepository extends CrudRepository<YeshteryUserTokensEntity, Long> {

    YeshteryUserTokensEntity findByToken(String token);

    @Transactional
    @Modifying
    void deleteByToken(String token);

    @Query("select t from YeshteryUserTokensEntity t left join fetch t.yeshteryUserEntity " +
            " where t.token = :token")
    YeshteryUserTokensEntity getUserEntityByToken(@Param("token") String token);

    @Query("select count( distinct t.yeshteryUserEntity) from YeshteryUserTokensEntity t " +
            " where t.yeshteryUserEntity.organizationId = :orgId and t.updateTime >= :startDate")
    Long countActiveUsers(@Param("orgId") Long orgId, @Param("startDate") LocalDateTime startDate);

    YeshteryUserTokensEntity saveAndFlush(YeshteryUserTokensEntity token);

    @Transactional
    @Modifying
    @Query(value = "delete from YeshteryUserTokensEntity t where t.yeshteryUserEntity = :user")
    void deleteByUserEntity(@Param("user") YeshteryUserEntity usr);

}
