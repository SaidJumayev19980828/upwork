package com.nasnav.dao;

import com.nasnav.persistence.SubPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubPostEntityRepository extends JpaRepository<SubPostEntity, Long> {

}
