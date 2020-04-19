package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ShopFloorDTO;
import com.nasnav.dto.ShopScenesDTO;
import com.nasnav.dto.ShopSectionsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Table(name = "shop_floors")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopFloorsEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private Integer number;

    @Column(name = "name")
    private String name;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop360_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopThreeSixtyEntity shopThreeSixtyEntity;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organizationEntity;

    @OneToMany(mappedBy = "shopFloorsEntity", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ShopSectionsEntity> shopSections;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopFloorDTO floor = new ShopFloorDTO();
        floor.setId(getId());
        floor.setNumber(getNumber());
        floor.setName(getName());
        floor.setShopSections(getShopSections().stream()
                                               .map(section -> (ShopSectionsDTO) section.getRepresentation())
                                               .sorted(comparing(ShopSectionsDTO::getId))
                                               .collect(Collectors.toList()));
        return floor;
    }
}
