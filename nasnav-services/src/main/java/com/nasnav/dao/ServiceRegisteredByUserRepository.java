package com.nasnav.dao;


import com.nasnav.persistence.ServiceRegisteredByUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRegisteredByUserRepository  extends JpaRepository<ServiceRegisteredByUser,Long> {
}
