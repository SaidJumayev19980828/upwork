package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.FileEntity;

public interface FilesRepository extends JpaRepository<FileEntity, Long> {

	boolean existsByUrl(String url);

	boolean existsByLocation(String uniqueLocation);

	FileEntity findByUrl(String url);

	Long countByUrl(String uri);
	
}
