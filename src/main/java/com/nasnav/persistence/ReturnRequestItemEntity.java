package com.nasnav.persistence;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="return_request_item")
@Data
public class ReturnRequestItemEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="return_request_id")
    private ReturnRequestEntity returnRequest;

    @ManyToOne
    @JoinColumn(name="order_item_id")
    private BasketsEntity basket;

    @Column(name = "returned_quantity")
    private Integer returnedQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @ManyToOne
    @JoinColumn(name="received_by")
    private EmployeeUserEntity receivedBy;

    @Column(name="received_on")
    private LocalDateTime receivedOn;

    @ManyToOne
    @JoinColumn(name="created_by_user")
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name="created_by_employee")
    private EmployeeUserEntity createdByEmployee;

}
