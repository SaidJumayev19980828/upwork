package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represent APP configuration properties like email config.
 */

@Entity
@Table(name = "configurations")
@Data
public class Configuration implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String key;

    @Column(name = "config_value")
    private String value;

    public Configuration() {
    }

    public Configuration(String key) {
        this.key = key;
        this.value = new String();
    }

}

