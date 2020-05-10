package com.nasnav.cache;

import java.nio.ByteBuffer;

import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;



/**
 * ehCache serializer.
 * Used to serialize/deserialize cached objects using kyro library.
 * */
public class KryoSerializer implements Serializer<Object> {

  private static final int SERIALIZE_BUFFER_SIZE = 8192;
  private static final Kryo kryo = new Kryo();

  public KryoSerializer(ClassLoader classLoader) {
	  //by default kyro requires each class it will serialize to be "registerd" to improve serialization 
	  //performance. as it is hard to manage which classes will be cached, we just register every thing.
	  //this can cause some issues, but we don't think it will affect us here
	  //https://github.com/EsotericSoftware/kryo/issues/196
	  kryo.setRegistrationRequired(false);	
	  
	  
	  //set the class loader to the application classloader
	  //this is to solve problems with devtools, because it creates two classloaders , one for static libraries
	  //and another for application classes.
	  //if kyro classloader is different from the application, we may get cast exceptions at deserialization.
	  //https://github.com/AxonFramework/AxonFramework/issues/344#issuecomment-310308359
	  if (classLoader != null) {
		    kryo.setClassLoader(classLoader);
	  }
  }

  
  
  
  @Override
  public ByteBuffer serialize(final Object object) throws SerializerException {
    Output output = new Output(SERIALIZE_BUFFER_SIZE);
    kryo.writeClassAndObject(output, object);
    return ByteBuffer.wrap(output.getBuffer());
  }

  
  
  
  @Override
  public Object read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    Input input =  new Input(new ByteBufferInputStream(binary)) ;
    return kryo.readClassAndObject(input);
  }

  
  
  
  @Override
  public boolean equals(final Object object, final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    return object.equals(read(binary));
  }

}

