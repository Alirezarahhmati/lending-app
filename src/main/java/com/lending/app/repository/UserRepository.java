package com.lending.app.repository;

import com.lending.app.model.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    @Query(
            value = "SELECT * FROM users " +
                    "WHERE username = :username " +
                    "AND deleted_at IS NULL " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<User> findByUsername(@Param("username") String username);

    @Query(
            value = "SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END " +
                    "FROM users " +
                    "WHERE username = :username " +
                    "AND deleted_at IS NULL",
            nativeQuery = true
    )
    boolean existsByUsername(@Param("username") String username);

    @Query(
            value = "SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END " +
                    "FROM users " +
                    "WHERE email = :email " +
                    "AND deleted_at IS NULL",
            nativeQuery = true
    )
    boolean existsByEmail(@Param("email") String email);

    @Override
    @Query(
            value = "SELECT * FROM users " +
                    "WHERE id = :id " +
                    "AND deleted_at IS NULL " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<User> findById(@Param("id") String id);

    @Override
    @Query(
            value = "SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END " +
                    "FROM users " +
                    "WHERE id = :id " +
                    "AND deleted_at IS NULL",
            nativeQuery = true
    )
    boolean existsById(@Param("id") String id);

    @Override
    @Query(
            value = "SELECT * FROM users " +
                    "WHERE deleted_at IS NULL",
            nativeQuery = true
    )
    List<User> findAll();

    @Query(
            value = "UPDATE users " +
                    "SET deleted_at = CURRENT_TIMESTAMP, " +
                    "version = version + 1 " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    @Modifying
    void softDeleteById(@Param("id") String id);

    @Query(
            value = "SELECT version FROM users " +
                    "WHERE id = :id " +
                    "AND deleted_at IS NULL " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<Long> findVersionById(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value ="5000")})
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdForUpdate(@Param("id") String id);

}