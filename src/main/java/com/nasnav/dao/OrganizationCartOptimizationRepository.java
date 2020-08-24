package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OrganizationCartOptimization;

public interface OrganizationCartOptimizationRepository extends JpaRepository<OrganizationCartOptimization, Long> {

	Optional<OrganizationCartOptimization> findByOptimizationStrategyAndOrganization_Id(String strategy, long l);

}
