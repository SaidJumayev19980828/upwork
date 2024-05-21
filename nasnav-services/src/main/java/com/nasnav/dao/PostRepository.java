package com.nasnav.dao;

import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.impl.PostServiceImpl;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Optional<PostEntity> findByIdAndType(long id, int type);
    PageImpl<PostEntity> getAllByUserAndStatus(UserEntity userId, int status, Pageable page);
    PageImpl<PostEntity> getAllByUser_IdAndType(long userId, int type, Pageable page);
    PageImpl<PostEntity> getAllByOrganization_IdAndStatusAndType(long orgId, int status, int type, Pageable page);
    PageImpl<PostEntity> getAllByOrganization_IdAndType(long orgId, int type, Pageable page);
    PageImpl<PostEntity> getAllByUserInAndStatus(List<UserEntity> users, int status, Pageable page);
    Long countAllByUser_IdAndStatus(Long userId, int status);

    @EntityGraph(attributePaths = {"products","advertisement.organization.bankAccount","user"})
    PageImpl<PostEntity> findAllByAdvertisementIsNotNullAndAdvertisement_FromDateLessThanEqualAndAdvertisement_ToDateGreaterThanEqual(LocalDateTime from, LocalDateTime to, Pageable pageable);

    PageImpl<PostEntity> getAllBySavedByUsersContains(UserEntity userId, Pageable page);

    @Query("select distinct p as postEntity, count(l) as count from PostEntity p left join p.subPosts sp left join sp.likes l group by p order by count(l) desc")
    PageImpl<PostServiceImpl.TrendyPostRep> findAllTrendyPosts(Pageable page);

    @Query("select distinct p as postEntity, COUNT(l) as count from PostEntity p left join p.subPosts sp left join sp.likes l where p.createdAt>= :oneWeekAgo "
            + "group by p order by count(l) desc,p.createdAt desc")
    PageImpl<PostServiceImpl.TrendyPostRep> findTrendyPostsOfTheWeek(LocalDateTime oneWeekAgo, Pageable page);
}
