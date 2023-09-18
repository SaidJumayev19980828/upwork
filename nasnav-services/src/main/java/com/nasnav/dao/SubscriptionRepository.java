package com.nasnav.dao;

import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

}
