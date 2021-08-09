package com.nasnav.dao;

import com.nasnav.persistence.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilesRepository extends JpaRepository<FileEntity, Long> {

	boolean existsByUrl(String url);

	boolean existsByLocation(String uniqueLocation);

	FileEntity findByUrl(String url);
	FileEntity findByUrlEndsWithAndOrganization_Id(String url, Long orgId);

	List<FileEntity> findByOrganization_IdAndMimetypeContaining(Long id, String mimeType);

	List<FileEntity> findByUrlInAndMimetypeContaining(List<String> urls, String mimeType);

	Long countByUrl(String uri);
	
}
