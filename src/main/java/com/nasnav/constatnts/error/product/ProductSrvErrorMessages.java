package com.nasnav.constatnts.error.product;

public class ProductSrvErrorMessages {
	public static final String ERR_PRODUCT_READ_FAIL = "Failed to fetch the data of product[%d]";
	public static final String ERR_PRODUCT_HAS_NO_VARIANTS = "Product with id[%d] doesn't have any variants! A product must have at least one variant.";
	public static final String ERR_CANNOT_DELETE_BUNDLE_ITEM = "Cannot delete Product with id[%d]! the product is inside bundles [%s] and must be removed first!";
	public static final String ERR_CANNOT_DELETE_PRODUCT_BY_OTHER_ORG_USER = "Product of ID[%d] cannot be deleted by a user from oraganization of id [%d]";
	public static final String ERR_PRODUCT_STILL_USED = "Failed to delete product with id[%d]! Product is still used in the system (stocks, orders, bundles, ...)!";
	public static final String ERR_PRODUCT_DELETE_FAILED = "Failed to delete product with id[%d]!";
	public static final String ERR_PRODUCT_HAS_NO_DEFAULT_STOCK = "Couldn't retrieve default stock for Product of id[%]";
}
