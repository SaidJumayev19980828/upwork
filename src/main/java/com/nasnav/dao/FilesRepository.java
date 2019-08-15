package com.nasnav.dao;

import java.nio.file.Path;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.FilesEntity;

public interface FilesRepository extends JpaRepository<FilesEntity, Long> {

	boolean existsByUrl(String url);

	boolean existByLocation(String uniqueLocation);

}
