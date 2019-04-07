package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Table(name = "countries")
@Entity
@Data
public class CountriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;

//    @OneToOne(mappedBy = "countriesEntity")
//    @JsonIgnore
//    private CitiesEntity citiesEntity;

}
