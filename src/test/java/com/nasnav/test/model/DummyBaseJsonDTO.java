package com.nasnav.test.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseJsonDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DummyBaseJsonDTO extends BaseJsonDTO {
	private String notReallyNeeded;
	private String importantProp;
	private String propForCreatingData;
	private String propForUpdatingData1;
	private String propForUpdatingData2;

	
	
	@Override
	protected void initRequiredProperties() {		
		setPropertyAsRequired("importantProp");
		setPropertyAsRequiredForNewData("propForCreatingData");
		setPropertyAsRequiredForUpdatedData("propForUpdatingData1");
		setPropertyAsRequiredForUpdatedData("propForUpdatingData2");		
	}
	
	
	//must modify the setters to set the property as updated :/
	public void setNotReallyNeeded(String notReallyNeeded) {
		setPropertyAsUpdated("notReallyNeeded");
		this.notReallyNeeded = notReallyNeeded;
	}

	public void setImportantProp(String importantProp) {
		setPropertyAsUpdated("importantProp");
		this.importantProp = importantProp;
	}

	public void setPropForCreatingData(String propForCreatingData) {
		setPropertyAsUpdated("propForCreatingData");
		this.propForCreatingData = propForCreatingData;
	}

	public void setPropForUpdatingData1(String propForUpdatingData1) {
		setPropertyAsUpdated("propForUpdatingData1");
		this.propForUpdatingData1 = propForUpdatingData1;
	}

	public void setPropForUpdatingData2(String propForUpdatingData2) {
		setPropertyAsUpdated("propForUpdatingData2");
		this.propForUpdatingData2 = propForUpdatingData2;
	}

}