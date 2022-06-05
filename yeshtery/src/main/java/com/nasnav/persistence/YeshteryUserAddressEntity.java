package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Table(name = "yeshtery_user_addresses")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class YeshteryUserAddressEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "yeshtery_user_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private YeshteryUserEntity user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AddressesEntity address;

    private boolean principal;

    @Override
    public BaseRepresentationObject getRepresentation() {
        AddressRepObj obj = (AddressRepObj)this.address.getRepresentation();
        obj.setPrincipal(this.principal);
        return obj;
    }
}
