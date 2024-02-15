package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.WebScrapingLog;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebScrapingLogRepository extends JpaRepository<WebScrapingLog, Long> {
    PageImpl<WebScrapingLog> findAllByOrganizationOrderByCreatedAtDesc(OrganizationEntity organization, Pageable pageable);
}