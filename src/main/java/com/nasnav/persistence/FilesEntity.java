package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class FilesEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String url;
	private String location;
	private String mimetype;
	
	@Column(name= "orig_filename")
	private String originalFileName;
	
	@ManyToOne
	@JoinColumn(name= "organization_id")
	private OrganizationEntity organization;
}
