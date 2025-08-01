package com.feather.api.jpa.repository;

import java.util.Optional;

import com.feather.api.jpa.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link User} entities.
 * Extends {@link CrudRepository} to provide CRUD operations.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Retrieves a User by its email
     *
     * @param email email to search for
     * @return User
     */
    Optional<User> findByEmail(String email);

}

