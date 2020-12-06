package com.nasnav.dao;

import com.nasnav.persistence.FileEntity;
import com.nasnav.persistence.FilesResizedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FilesResizedRepository extends JpaRepository<FilesResizedEntity, Long> {

    List<FilesResizedEntity> findByOriginalFileAndHeightAndWidth(FileEntity file, Integer height, Integer width);
    List<FilesResizedEntity> findByOriginalFileAndHeightAndWidthIsNull(FileEntity file, Integer height);
    List<FilesResizedEntity> findByOriginalFileAndWidthAndHeightIsNull(FileEntity file, Integer width);

    @Transactional
    @Modifying
    @Query("delete from FilesResizedEntity r where r.originalFile = :file")
    void deleteByOriginalFile(@Param("file") FileEntity file);
}
