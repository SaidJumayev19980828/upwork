package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

@Table(name = "tags")
@Entity
@Data
public class TagsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
}
