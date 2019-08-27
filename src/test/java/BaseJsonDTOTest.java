import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.JSONObject;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.test.model.DummyBaseJsonDTO;

public class BaseJsonDTOTest {
	
	@Test
	public void requiredPropertiesInitTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		
		Set<PropertyDescriptor> required = dto.getRequiredPropertiesAlways();
		Set<PropertyDescriptor> requiredForCreate = dto.getRequiredPropertiesForDataCreate();
		Set<PropertyDescriptor> requiredForUpdate = dto.getRequiredPropertiesForDataUpdate();
		
		assertEquals(1 , required.size() );
		assertEquals( "Properties required for create includes properties that are always required"
				,2 
				, requiredForCreate.size());
		
		assertEquals( "Properties required for update includes properties that are always required"
				,3 
				, requiredForUpdate.size());
		
		assertEquals("check the properties that were set as required for all cases"
				, "importantProp"
				, required.stream().findFirst().get().getName());
		
		
		assertTrue("check the properties that were set as required for create"
				, Arrays.asList("importantProp", "propForCreatingData")
						.stream()
						.allMatch(val -> anyPropertyHasName(requiredForCreate, val)));
		
		assertTrue("check the properties that were set as required for Upate"
				, Arrays.asList("importantProp", "propForUpdatingData1", "propForUpdatingData2")
						.stream()
						.allMatch(val -> anyPropertyHasName(requiredForUpdate,val)));
		
		assertTrue(dto.areRequiredForDataCreatePropertiesPresent());
		assertTrue(dto.arePropertiesRequiredForCreateNotNull());
		assertTrue("all properties were updated and had values"
				, dto.areRequiredForCreatePropertiesProvided());
		
		
		assertTrue(dto.areRequiredForDataUpdatePropertiesPresent());
		assertTrue(dto.arePropertiesRequiredForUpdateNotNull());
		assertTrue("all properties were updated and had values"
				, dto.areRequiredForUpdatePropertiesProvided());
	}


	
	
	
	@Test
	public void requiredPropertiesMissingTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.remove("important_prop");
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		// important_prop is always required
		assertFalse(dto.areRequiredAlwaysPropertiesPresent());
		
		assertFalse(dto.areRequiredForDataCreatePropertiesPresent());
		assertFalse(dto.areRequiredForCreatePropertiesProvided());
		
		assertFalse(dto.areRequiredForDataUpdatePropertiesPresent());
		assertFalse(dto.areRequiredForUpdatePropertiesProvided());	
	}
	
	
	
	
	@Test
	public void requiredForCreatePropertiesMissingTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.remove("prop_for_creating_data");
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		assertFalse(dto.areRequiredForDataCreatePropertiesPresent());
		assertFalse(dto.areRequiredForCreatePropertiesProvided());
		
		assertTrue(dto.areRequiredForDataUpdatePropertiesPresent());
		assertTrue(dto.areRequiredForUpdatePropertiesProvided());		
	}
	
	
	
	@Test
	public void requiredForUpdatePropertiesMissingTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.remove("prop_for_updating_data1");
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		assertTrue(dto.areRequiredForDataCreatePropertiesPresent());
		assertTrue(dto.areRequiredForCreatePropertiesProvided());
		
		assertFalse(dto.areRequiredForDataUpdatePropertiesPresent());
		assertFalse(dto.areRequiredForUpdatePropertiesProvided());		
	}
	
	
	
	@Test
	public void requiredForUpdatePropertiesIsNullTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.put("prop_for_updating_data1", JSONObject.NULL);
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		assertTrue(dto.areRequiredForDataCreatePropertiesPresent());
		assertTrue(dto.areRequiredForCreatePropertiesProvided());
		
		assertTrue(dto.areRequiredForDataUpdatePropertiesPresent());
		assertFalse(dto.isRequiredPropertyProvided("propForUpdatingData1"));
		assertFalse(dto.arePropertiesRequiredForUpdateNotNull());
		assertFalse(dto.areRequiredForUpdatePropertiesProvided());		
	}
	
	
	
	
	@Test
	public void requiredAlwaysPropertiesIsNullTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.put("important_prop", JSONObject.NULL);
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		assertTrue(dto.areRequiredForDataCreatePropertiesPresent());
		assertFalse(dto.arePropertiesRequiredForCreateNotNull());
		assertFalse(dto.areRequiredForCreatePropertiesProvided());
		
		assertTrue(dto.areRequiredForDataUpdatePropertiesPresent());
		assertFalse(dto.isRequiredPropertyProvided("importantProp"));
		assertFalse(dto.arePropertiesRequiredForUpdateNotNull());
		assertFalse(dto.areRequiredForUpdatePropertiesProvided());		
	}
	
	
	
	@Test
	public void missingOptionalDataTestTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.remove("not_really_needed");
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		assertTrue(dto.areRequiredForDataCreatePropertiesPresent());
		assertTrue(dto.arePropertiesRequiredForCreateNotNull());
		assertTrue("all properties were updated and had values"
				, dto.areRequiredForCreatePropertiesProvided());
		
		
		assertTrue(dto.areRequiredForDataUpdatePropertiesPresent());
		assertTrue(dto.arePropertiesRequiredForUpdateNotNull());
		assertTrue("all properties were updated and had values"
				, dto.areRequiredForUpdatePropertiesProvided());
		
		assertFalse(dto.isUpdated("notReallyNeeded"));
	}
	
	
	
	@Test
	public void NullOptionalDataTestTest() throws Exception, JsonProcessingException, IOException {
		JSONObject json = createDummyJson();
		json.put("not_really_needed", JSONObject.NULL);
		
		ObjectMapper mapper = new ObjectMapper();
		DummyBaseJsonDTO dto = mapper.readValue(json.toString(), DummyBaseJsonDTO.class);
		
		assertTrue(dto.areRequiredForDataCreatePropertiesPresent());
		assertTrue(dto.arePropertiesRequiredForCreateNotNull());
		assertTrue("all properties were updated and had values"
				, dto.areRequiredForCreatePropertiesProvided());
		
		
		assertTrue(dto.areRequiredForDataUpdatePropertiesPresent());
		assertTrue(dto.arePropertiesRequiredForUpdateNotNull());
		assertTrue("all properties were updated and had values"
				, dto.areRequiredForUpdatePropertiesProvided());
		
		assertTrue(dto.isUpdated("notReallyNeeded"));
	}
	
	
	

	private JSONObject createDummyJson() {
		JSONObject json = new JSONObject();
		json.put("not_really_needed", "value");
		json.put("important_prop", "Not Null");
		json.put("prop_for_creating_data", "new value");
		json.put("prop_for_updating_data1", "value2");
		json.put("prop_for_updating_data2", "value3");
		return json;
	}
	
	

	private boolean anyPropertyHasName(Set<PropertyDescriptor> requiredProps, String name) {
		return requiredProps
			.stream()
			.map(PropertyDescriptor::getName)
			.anyMatch(name::equals);
	}

}







