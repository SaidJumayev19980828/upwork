package com.nasnav.persistence;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "roles")
@EqualsAndHashCode(callSuper=false)
public class Role extends DefaultBusinessEntity<Integer>{

    @Column(name = "name")
    private String name;

    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "created_at")
   public LocalDateTime createdAt;

   @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
