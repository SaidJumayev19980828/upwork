package com.nasnav.constatnts.error.orders;

public class OrderServiceErrorMessages {
	
	public static final String ERR_UPDATED_ORDER_WITH_NO_ID = "No Id was provided for the order to update!";
	public static final String ERR_NEW_ORDER_WITH_EMPTY_BASKET = "New orders cannot have empty baskets!";
	public static final String ERR_NEW_ORDER_WITH_ID = "New Orders cannot have a non-zero order Id!";
	public static final String ERR_INVALID_ITEM_QUANTITY = "An item has invalid quantity!";
	public static final String ERR_NULL_ITEM = "Null Basket Item!";
	public static final String ERR_NON_EXISTING_STOCK_ID = "Invalid Basket Item with non-existing Stock!";
	public static final String ERR_INVALID_BASKET_ITEM = "Invalid Basket item!";
	public static final String ERR_NO_ENOUGH_STOCK = "Basket Item has insufficient stock!";
	public static final String ERR_ITEMS_FROM_MULTIPLE_SHOPS = "Basket items belong to multiple shops!";
	public static final String ERR_INVALID_ORDER_STATUS = "Invalid Order status!";
	public static final String ERR_ORDER_NOT_EXISTS = "No Order exists with id[%s]";
	public static final String ERR_CALC_ORDER_FAILED = "Failed to calculate order value!";
	public static final String ERR_INVALID_ORDER_STATUS_UPDATE = "Cannot update order staus to previous status";
}
