package com.nasnav.persistence;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Table(name = "addons")
@Entity
@Data
public class AddonEntity {
	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(columnDefinition = "serial")
    private Long id;
	
	@Column(name = "name")
    private String name;
    
	@Column(name = "type")
    private Integer type;
 
 

    
    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organizationEntity;

    @ManyToMany(mappedBy = "addons")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private Set<ProductEntity> products;
    
    
}
