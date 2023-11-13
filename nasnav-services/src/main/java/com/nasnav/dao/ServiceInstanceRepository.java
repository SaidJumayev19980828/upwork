package com.nasnav.dao;

import com.nasnav.persistence.ServiceInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceInstanceRepository extends JpaRepository<ServiceInstanceEntity, Long> {

}
