package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.dto.AddonDetailsDTO;
import com.nasnav.persistence.AddonBasketEntity;

public interface AddonsBasketRepository extends CrudRepository<AddonBasketEntity, Long> {

	@Query(value = "select NEW com.nasnav.dto.AddonDetailsDTO(st.id,a.name,a.id,st.price,a.type) from AddonBasketEntity dt, AddonStocksEntity st ,  AddonEntity a where st.id=dt.stocksEntity.id and a.id=st.addonEntity.id and dt.basketEntity.id = :itemId")
    List<AddonDetailsDTO> listItemAddons( @Param("itemId")Long itemId);

}
