package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Table(name = "categories")
@Entity
@Data
public class CategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "p_name")
    private String pname;
    private String logo;

//    @OneToOne(mappedBy = "categoriesEntity")
//    @JsonIgnore
//    private ProductEntity productEntity;

}
