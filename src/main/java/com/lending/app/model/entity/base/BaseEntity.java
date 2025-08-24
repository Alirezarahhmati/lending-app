package com.lending.app.model.entity.base;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, unique = true)
    private String id;

    @Column(nullable = false, updatable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;

    public BaseEntity() {
        this.id = new ULID().nextULID();
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now().toString();
    }
}
