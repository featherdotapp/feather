package com.feather.api.jpa.repository;

import java.util.Optional;

import com.feather.api.jpa.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    void updateAccessTokenById(Long id, String accessToken);

    void updateRefreshTokenById(Long id, String refreshToken);
}

