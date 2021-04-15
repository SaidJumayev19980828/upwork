package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, Long> {

    @Query("select sub.email from UserSubscriptionEntity sub left join sub.organization org " +
            " where org = :org and sub.token is null")
    List<String> findEmailsByOrganizationAndTokenNull(OrganizationEntity org);

    UserSubscriptionEntity findByEmailAndOrganization_Id(String email, Long orgId);

    UserSubscriptionEntity findByToken(String token);
    boolean existsByToken(String token);
    boolean existsByEmailAndOrganization_Id(String email, Long orgId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserSubscriptionEntity sub where sub.email = :email and sub.organization = :org and sub.token is null")
    void deleteByEmailAndOrganizationAndTokenNull(@Param("email") String email,
                                                  @Param("org")OrganizationEntity org);
}
