package com.nasnav.integration.events.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImportEventParam {
	private Integer pageNum;
	private Integer PageCount;
}
