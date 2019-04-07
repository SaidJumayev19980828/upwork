package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class DefaultBusinessEntity<T extends Serializable> extends AbstractPersistable<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private T id;

//    Eventually, the "created_at" and "updated_at" will be removed from some of the columns as it is not needed there.
//    Also, this would overwrite any meaningful data

//    @Column(name = "created_at")
//    public LocalDateTime createdAt;

//    @Column(name = "updated_at")
//    public LocalDateTime updatedAt;

//    protected DefaultBusinessEntity() {
//        this.id = null;
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
}
