package com.lending.app.model.entity;

import com.lending.app.model.entity.base.BaseEntity;
import jakarta.persistence.*;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_username_deleted", columnNames = {"username", "deleted_at"}),
                @UniqueConstraint(name = "uk_email_deleted", columnNames = {"email", "deleted_at"})
        },
        indexes = {
                @Index(name = "idx_username_deleted", columnList = "username, deleted_at"),
                @Index(name = "idx_email_deleted", columnList = "email, deleted_at"),
                @Index(name = "idx_deleted_at", columnList = "deleted_at")
        }
)
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int score;

    private LocalDateTime deletedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

}
