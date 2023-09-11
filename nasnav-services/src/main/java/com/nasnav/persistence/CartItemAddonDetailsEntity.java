package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "cart_item_addon_details")
@Data
public class CartItemAddonDetailsEntity {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(columnDefinition = "serial")
	    private Long id;
	    
	    @ManyToOne
	    @JoinColumn(name="addon_stock_id", referencedColumnName = "ID")
	    private AddonStocksEntity addonStockEntity;
	    
	    
	    @ManyToOne
	    @JoinColumn(name="cart_item_id", referencedColumnName = "ID")
	    private CartItemEntity cartItemEntity; 
	    

	    
	    @ManyToOne
	    @JoinColumn(name="USER_ID")
	    private UserEntity user;
	    
	    
	    
	    
	    
	    
	    
	    

}
