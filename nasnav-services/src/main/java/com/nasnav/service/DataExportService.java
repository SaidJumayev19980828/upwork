package com.nasnav.service;

import com.nasnav.service.model.importproduct.csv.CsvRow;

import java.util.List;

public interface DataExportService {

	List<CsvRow> exportProductsData(Long orgId, Long shopId);
}
