package com.nasnav.persistence;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author ahmed.bastawesy
 */
@Data
@Entity
@Table(name = "roles")
public class Role extends DefaultBusinessEntity<Integer>{


    @Column(name = "name")
    private String name;

    @Column(name = "organization_id")
    private Integer organizationId;
}
