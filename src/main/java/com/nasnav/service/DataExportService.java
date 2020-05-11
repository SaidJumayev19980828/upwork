package com.nasnav.service;

import java.util.List;

import com.nasnav.service.model.importproduct.csv.CsvRow;

public interface DataExportService {

	List<CsvRow> exportProductsData(Long orgId, Long shopId);
}
