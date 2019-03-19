package com.nasnav.dao;

import com.nasnav.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findByEmail(String email);
//    List<UserEntity> findById(Long id);

    /**
     * Ensure that the new email is not registered to another user
     *
     * @param email email to be checked
     * @return true if the passed email parameter exists
     */
    boolean existsByEmail(String email);
}



