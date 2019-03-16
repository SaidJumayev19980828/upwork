package com.nasnav.repository;

import com.nasnav.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Ensure that the new email is not registered to another user
     *
     * @param email email to be checked
     * @return true if the passed email parameter exists
     */
    boolean existsByEmail(String email);
}
