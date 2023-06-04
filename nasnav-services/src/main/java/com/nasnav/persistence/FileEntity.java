package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;


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
	@ManyToOne
	@JoinColumn(name= "user_id")
	private UserEntity userId;
}
