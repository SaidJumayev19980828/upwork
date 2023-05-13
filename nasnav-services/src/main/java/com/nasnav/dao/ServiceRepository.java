package com.nasnav.dao;

import com.nasnav.persistence.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Services , Long> {
}
