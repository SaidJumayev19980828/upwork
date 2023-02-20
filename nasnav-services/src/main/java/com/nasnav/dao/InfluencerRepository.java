package com.nasnav.dao;

import com.nasnav.persistence.InfluencerEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface InfluencerRepository extends CrudRepository<InfluencerEntity, Long> {
    InfluencerEntity getByUser_IdOrEmployeeUser_Id(Long userId, Long employeeId);
    boolean existsByUser_IdOrEmployeeUser_Id(Long userId, Long employeeId);
    @Query("select influencer from InfluencerEntity influencer where :status is null or influencer.approved = :status")
    PageImpl<InfluencerEntity> findAllPageable(Boolean status, Pageable page);
}
