package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.FileEntity;

import java.util.List;

public interface FilesRepository extends JpaRepository<FileEntity, Long> {

	boolean existsByUrl(String url);

	boolean existsByLocation(String uniqueLocation);

	FileEntity findByUrl(String url);

	List<FileEntity> findByOrganization_IdAndMimetypeContaining(Long id, String mimeType);

	List<FileEntity> findByUrlInAndMimetypeContaining(List<String> urls, String mimeType);

	Long countByUrl(String uri);
	
}
