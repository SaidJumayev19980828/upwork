package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dto.AddonDetailsDTO;
import com.nasnav.persistence.CartItemAddonDetailsEntity;
import com.nasnav.persistence.CartItemEntity;

public interface CartItemAddonDetailsRepository extends JpaRepository<CartItemAddonDetailsEntity, Long> { 
	
	@Transactional
	@Modifying
	@Query("delete from CartItemAddonDetailsEntity cr where cr.cartItemEntity.id=:id ")
	public void deleteByCartItemEntity_Id(@Param("id") Long cartItemId);
	
	CartItemAddonDetailsEntity findByCartItemEntity_IdAndAddonStockEntity_Id(Long cartItemId, Long addonStockId);	
	
	Boolean existsByAddonStockEntity_Id(Long addonStockId);
	@Transactional
	@Modifying
	@Query("delete from CartItemAddonDetailsEntity cr where cr.user.id=:id ")
	public void deleteByUserId(@Param("id") Long userId);
	
	@Query(value = "select NEW com.nasnav.dto.AddonDetailsDTO(st.id,a.name,a.id,st.price,a.type) from CartItemAddonDetailsEntity dt, AddonStocksEntity st ,  AddonEntity a where st.id=dt.addonStockEntity.id and a.id=st.addonEntity.id and dt.cartItemEntity.id = :itemId")
    List<AddonDetailsDTO> listItemAddons( @Param("itemId")Long itemId);
	
	@Query("select dt from CartItemAddonDetailsEntity dt" +
			" , AddonStocksEntity st" +
			 "  ,UserEntity u"+
			" where  dt.user.id=:userId and u.id=dt.user.id and st.id=dt.addonStockEntity.id and st.quantity = 0")
	List<CartItemAddonDetailsEntity> findOutOfStockCartItemsAddons(@Param("userId") Long userId);
	
}