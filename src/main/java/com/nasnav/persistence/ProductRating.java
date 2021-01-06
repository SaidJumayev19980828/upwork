package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product_ratings")
@EqualsAndHashCode(callSuper=false)
public class ProductRating implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "variant_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductVariantsEntity variant;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    private Integer rate;
    private String review;
    @Column(name = "submission_date")
    @CreationTimestamp
    private LocalDateTime submissionDate;
    private Boolean approved;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ProductRateRepresentationObject obj = new ProductRateRepresentationObject();
        obj.setId(getId());
        obj.setVariantId(variant.getId());
        obj.setRate(getRate());
        obj.setReview(getReview());
        obj.setSubmissionDate(getSubmissionDate());
        obj.setUserId(getUser().getId());
        obj.setUserName(getUser().getName());
        obj.setApproved(approved.booleanValue());
        return obj;
    }
}
