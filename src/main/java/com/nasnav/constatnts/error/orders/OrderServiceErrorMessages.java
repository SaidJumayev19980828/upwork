package com.nasnav.constatnts.error.orders;

public class OrderServiceErrorMessages {
	
	public static final String ERR_UPDATED_ORDER_WITH_NO_ID = "No Id was provided for the order to update!";
	public static final String ERR_NEW_ORDER_WITH_EMPTY_BASKET = "New orders cannot have empty baskets!";
	public static final String ERR_NEW_ORDER_WITH_ID = "New Orders cannot have a non-zero order Id!";
	public static final String ERR_INVALID_ITEM_QUANTITY = "An item has invalid quantity!";
	public static final String ERR_NULL_ITEM = "Null Basket Item!";
	public static final String ERR_NON_EXISTING_STOCK_ID = "Invalid Basket Item with non-existing Stock!";
	public static final String ERR_INVALID_BASKET_ITEM = "Invalid Basket item!";
}
