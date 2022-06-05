package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyPointTypeDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "loyalty_point_types")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyPointTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public LoyaltyPointTypeDTO getRepresentation() {
        return new LoyaltyPointTypeDTO(this.id, this.name);
    }
}
