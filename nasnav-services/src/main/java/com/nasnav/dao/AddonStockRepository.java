package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.dto.AddonStocksDTO;
import com.nasnav.persistence.AddonStocksEntity;

public interface AddonStockRepository extends JpaRepository<AddonStocksEntity, Long> {


    Boolean existsByShopsEntity_IdAndAddonEntity_Id(Long shopId, Long addonId);
    Boolean existsByAddonEntity_Id(Long addonId);

    Optional<AddonStocksEntity> findByIdAndShopsEntity_IdAndAddonEntity_Id(Long id,Long shopId, Long addonId);
    
    AddonStocksEntity findByShopsEntity_IdAndAddonEntity_Id(Long shopId, Long addonId);

  
    List<AddonStocksEntity> findByShopsEntity_Id(Long shopId);
    void deleteByAddonEntity_Id(Long addonId);

    @Query(value = "select NEW com.nasnav.dto.AddonStocksDTO(st.id,a.name,a.id,s.id,s.name) from AddonStocksEntity st ,  ShopsEntity s ,  AddonEntity a where s.id=st.shopsEntity.id and a.id=st.addonEntity.id and s.id = :shopId")
    List<AddonStocksDTO> listStocks( @Param("shopId")Long shopId);
    

}
