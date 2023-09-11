package com.nasnav.dao;

import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    PageImpl<PostEntity> getAllByUser_IdAndStatus(long userId, int status, Pageable page);
    PageImpl<PostEntity> getAllByOrganization_IdAndStatusAndType(long orgId, int status, int type, Pageable page);
    PageImpl<PostEntity> getAllByOrganization_IdAndType(long orgId, int type, Pageable page);
    PageImpl<PostEntity> getAllByUserInAndStatus(List<UserEntity> users, int status, Pageable page);
    Long countAllByUser_IdAndStatus(Long userId, int status);
    PageImpl<PostEntity> findAllByAdvertisementIsNotNullAndAdvertisement_FromDateLessThanEqualAndAdvertisement_ToDateGreaterThanEqual(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
