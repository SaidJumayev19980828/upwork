package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="return_request")
@Data
public class ReturnRequestEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="created_on")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name="created_by_user")
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name="created_by_employee")
    private EmployeeUserEntity createdByEmployee;

    @ManyToOne
    @JoinColumn(name="meta_order_id")
    private MetaOrderEntity metaOrder;

    @Column(name = "status")
    private Integer status;

    @OneToMany(mappedBy = "returnRequest", cascade = ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ReturnRequestItemEntity> returnedItems;

    public ReturnRequestEntity() {
        returnedItems = new HashSet<>();
    }
}
