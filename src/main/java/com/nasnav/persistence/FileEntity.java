package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Entity
@Table(name= "FILES")
@EqualsAndHashCode(callSuper=false)
@Data
public class FileEntity {
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
