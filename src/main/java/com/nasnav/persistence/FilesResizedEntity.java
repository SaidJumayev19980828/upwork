package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name= "files_resized")
@EqualsAndHashCode(callSuper=false)
@Data
public class FilesResizedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_file_id", nullable = false)
    private FileEntity originalFile;

    private Integer width;

    private Integer height;

}
