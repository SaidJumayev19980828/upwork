package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "variant_feature_values")
@Data
@EqualsAndHashCode(callSuper=false)
public class VariantFeatureValueEntity extends AbstractPersistable<Integer> implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "variant_id", referencedColumnName = "id")
    @JsonIgnore
    private ProductVariantsEntity variant;

    @ManyToOne
    @JoinColumn(name = "feature_id", referencedColumnName = "id")
    @JsonIgnore
    private ProductFeaturesEntity feature;

    private String value;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }
}
