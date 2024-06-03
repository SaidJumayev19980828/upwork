package com.nasnav.dao;

import com.nasnav.persistence.InfluencerReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfluencerReferralRepository extends JpaRepository<InfluencerReferral, Long> {

    boolean existsByUserName(String userName);

    Optional<InfluencerReferral> findByUserName(String userName);

    Optional<InfluencerReferral> findByReferralReferralCode(String referralCede);

}
