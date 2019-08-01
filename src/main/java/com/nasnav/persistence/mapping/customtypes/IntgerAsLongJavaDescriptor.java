package com.nasnav.persistence.mapping.customtypes;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;

public class IntgerAsLongJavaDescriptor extends AbstractTypeDescriptor<Long> {
	
	
	private static final long serialVersionUID = 1L;
	
	public static final IntgerAsLongJavaDescriptor INSTANCE =  new IntgerAsLongJavaDescriptor();
		 
    public IntgerAsLongJavaDescriptor() {
        super(Long.class, ImmutableMutabilityPlan.INSTANCE);
    }

	protected IntgerAsLongJavaDescriptor(Class<Long> type) {
		super(type);
	}

	@Override
	public Long fromString(String string) {
		return Long.parseLong(string);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X unwrap(Long value, Class<X> type, WrapperOptions options) {
		if (value == null)
	        return null;
	 
	    if (Integer.class.isAssignableFrom(type))
	    	return (X) Integer.valueOf(value.intValue());
	 
	    throw unknownUnwrap(type);
	}

	@Override
	public <X> Long wrap(X value, WrapperOptions options) {
		if (value == null)
	        return null;
	 
	    if(Integer.class.isInstance(value))
	        return Long.valueOf((int)value);
	 
	    throw unknownWrap(value.getClass());
	}

}
