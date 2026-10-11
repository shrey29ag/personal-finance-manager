package com.example.finance.repository;

import com.example.finance.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for {@link Category}.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a default (global) category by name.
     *
     * @param name category name
     * @return the default category, if present
     */
    Optional<Category> findByNameAndUserIsNull(String name);

    /**
     * Finds a custom category owned by the given user.
     *
     * @param name   category name
     * @param userId owner id
     * @return the custom category, if present
     */
    Optional<Category> findByNameAndUserId(String name, Long userId);

    /**
     * Lists the default categories plus the given user's custom categories.
     *
     * @param userId owner id
     * @return visible categories, defaults first
     */
    @Query("select c from Category c where c.user is null or c.user.id = :userId order by c.custom asc, c.id asc")
    List<Category> findVisibleToUser(@Param("userId") Long userId);
}
