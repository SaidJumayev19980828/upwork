package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

@Data
@Table
@Entity(name = "organization_images_types")
public class ImageType {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column(name = "type_id", unique = true, nullable = false)
    private Long type_id;
    @Column(name = "organization_id")
    private Long organizationId;
    private String label;
    private String text;

    public void setType_id(Long type_id) {
        if(type_id !=null){
            this.type_id=type_id;
        }
    }
}
