package com.nasnav.persistence;

import com.nasnav.enumerations.Services;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "services_registered_in_package")
@Entity
@Builder
public class ServicesRegisteredInPackagesEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "services_num")
    private Long services;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private PackageEntity packageEntity;

}
