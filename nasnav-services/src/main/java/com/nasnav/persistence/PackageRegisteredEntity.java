package com.nasnav.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "package_registered")
@Entity
@Builder
public class PackageRegisteredEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private PackageEntity packageEntity;

    @Column(name = "registered_date")
    private Date registeredDate;


}
