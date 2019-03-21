package com.nasnav.persistence;

import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
public abstract class DefaultBusinessEntity<T extends Serializable> extends AbstractPersistable<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private T id;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    protected DefaultBusinessEntity() {
        this.id = null;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}
