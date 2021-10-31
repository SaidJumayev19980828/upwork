package com.nasnav.dao;

import com.nasnav.persistence.UserCharityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserCharityRepository extends JpaRepository<UserCharityEntity, Long> {

    List<UserCharityEntity> getByCharity_Id(Long charityId);

    Boolean existsByIdAndUser_IdAndCharity_Id(Long Id, Long userId, Long charityId);

    Optional<UserCharityEntity> findByUser_IdAndCharity_Id(Long userId, Long charityId);

    @Transactional
    @Modifying
    void deleteByUser_IdAndCharity_Id(Long userId, Long charityId);

    Optional<UserCharityEntity> findByIdAndUser_IdAndCharity_Id(Long id, Long userId, Long charityId);
}
