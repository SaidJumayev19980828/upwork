package com.nasnav.constatnts.error.dataimport;

public class ErrorMessages {
	public static final String ERR_PRODUCT_IMPORT_MISSING_PARAM = "Missing required parameters! required parameters are {shop_id, currency, encoding, headers, csv}";
	public static final String ERR_SHOP_ID_NOT_EXIST = "No shop exists with id [%d]";
	public static final String ERR_INVALID_ENCODING = "Encoding [%s] is not supported!";
	public static final String ERR_USER_CANNOT_CHANGE_SHOP_SETTINGS = "User that belongs to shop with id[%d] is not allowed to make changes to shop with id[%d]";
	public static final String ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP = "User that belongs to organization with id[%d] is not allowed to make changes to a shop from organization with id[%d]";
	public static final String ERR_NO_FILE_UPLOADED = "Uploaded file is invalid or empty!";
	public static final String ERR_PRODUCT_IMPORT_MISSING_HEADER_NAME = "One or more CSV header names was not provided!";
	public static final String ERR_CSV_PARSE_FAILURE = "Failed To parse Products CSV file!";
}
