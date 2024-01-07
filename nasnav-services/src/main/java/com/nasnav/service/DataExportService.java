package com.nasnav.service;

import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrdersListResponse;
import com.nasnav.service.model.importproduct.csv.CsvRow;

import java.util.List;

public interface DataExportService {

	List<CsvRow> exportProductsData(Long orgId, Long shopId);
	OrdersListResponse exportOrdersData(OrderSearchParam params);
}
