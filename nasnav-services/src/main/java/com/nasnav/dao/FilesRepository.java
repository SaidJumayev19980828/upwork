package com.nasnav.dao;

import com.nasnav.persistence.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FilesRepository extends JpaRepository<FileEntity, Long> {

	boolean existsByUrl(String url);

	boolean existsByLocation(String uniqueLocation);

	FileEntity findByUrl(String url);
	FileEntity findByUrlAndOrganization_Id(String url, Long orgId);

	List<FileEntity> findByOrganization_IdAndMimetypeContaining(Long id, String mimeType);

	List<FileEntity> findByUrlInAndMimetypeContaining(List<String> urls, String mimeType);

	Long countByUrl(String uri);
    @Query("select f.url from FileEntity f where f.modelId =:id")
	List<String> getUrlsByModelId(@Param("id") Long id);
	
}
