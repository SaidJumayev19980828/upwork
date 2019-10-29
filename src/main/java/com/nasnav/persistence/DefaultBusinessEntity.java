package com.nasnav.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class DefaultBusinessEntity<T extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private T id;

}
