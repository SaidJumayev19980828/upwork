package com.nasnav.persistence.mapping.customtypes;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;

public class IntgerAsLongType 
extends AbstractSingleColumnStandardBasicType<Long> {

  public static final IntgerAsLongType INSTANCE = new IntgerAsLongType();

  public IntgerAsLongType() {
      super(IntegerTypeDescriptor.INSTANCE, IntgerAsLongJavaDescriptor.INSTANCE);
  }

  @Override
  public String getName() {
      return "IntegerAsLong";
  }
}
