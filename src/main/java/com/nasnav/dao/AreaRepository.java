package com.nasnav.dao;

import com.nasnav.persistence.AreasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<AreasEntity, Long> {

}
