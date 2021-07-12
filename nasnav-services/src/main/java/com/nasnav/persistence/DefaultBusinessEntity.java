package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class DefaultBusinessEntity<T extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private T id;

}
