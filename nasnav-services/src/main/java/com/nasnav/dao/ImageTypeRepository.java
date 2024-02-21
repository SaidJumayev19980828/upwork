package com.nasnav.dao;

import com.nasnav.persistence.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageTypeRepository extends JpaRepository<ImageType, Long> {

    List<ImageType> findAllByOrganizationId(Long orgnizationId);
}
