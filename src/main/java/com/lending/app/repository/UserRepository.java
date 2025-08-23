package com.lending.app.repository;

import com.lending.app.model.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @Query(
            value = """
                    SELECT u.* 
                    FROM users u
                    WHERE u.username = :username 
                      AND u.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Optional<User> findByUsername(@Param("username") String username);

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1 
                        FROM users u
                        WHERE u.username = :username 
                          AND u.deleted_at IS NULL
                    )
                    """,
            nativeQuery = true
    )
    boolean existsByUsername(@Param("username") String username);

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1 
                        FROM users u
                        WHERE u.email = :email 
                          AND u.deleted_at IS NULL
                    )
                    """,
            nativeQuery = true
    )
    boolean existsByEmail(@Param("email") String email);

    @Override
    @Query(
            value = """
                    SELECT u.* 
                    FROM users u
                    WHERE u.id = :id 
                      AND u.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Optional<User> findById(@Param("id") String id);

    @Override
    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1 
                        FROM users u
                        WHERE u.id = :id 
                          AND u.deleted_at IS NULL
                    )
                    """,
            nativeQuery = true
    )
    boolean existsById(@Param("id") String id);

    @Override
    @Query(
            value = """
                    SELECT u.* 
                    FROM users u
                    WHERE u.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    List<User> findAll();

    @Modifying
    @Query(
            value = """
                    UPDATE users 
                    SET deleted_at = CURRENT_TIMESTAMP
                    WHERE id = :id
                    """,
            nativeQuery = true
    )
    void softDeleteById(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdForUpdate(@Param("id") String id);

}