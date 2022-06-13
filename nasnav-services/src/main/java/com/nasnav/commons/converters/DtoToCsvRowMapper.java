package com.nasnav.commons.converters;

import com.nasnav.service.model.importproduct.csv.CsvRow;

import java.util.Map;

public abstract class DtoToCsvRowMapper {
    protected Map<String,String> COLUMN_MAPPING;

    public abstract CsvRow map(Object dto);
    public abstract Map<String, String> getColumnMapping();
}
