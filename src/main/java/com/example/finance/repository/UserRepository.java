package com.example.finance.repository;

import com.example.finance.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username (email).
     *
     * @param username the username
     * @return the user, if present
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a username is already taken.
     *
     * @param username the username
     * @return true if a user with that username exists
     */
    boolean existsByUsername(String username);
}
