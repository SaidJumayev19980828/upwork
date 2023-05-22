package com.nasnav.dao;


import com.nasnav.persistence.ServiceRegisteredEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRegisteredByUserRepository  extends JpaRepository<ServiceRegisteredEntity,Long> {
}
