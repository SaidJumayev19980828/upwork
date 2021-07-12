package com.nasnav.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import java.nio.ByteBuffer;
import java.util.Objects;

import static com.nasnav.cache.CachingConfig.MAX_CACHED_OBJECT_SIZE_MB;



/**
 * ehCache serializer.
 * Used to serialize/deserialize cached objects using kyro library.
 * */
public class KryoSerializer implements Serializer<Object> {

  private static final int SERIALIZE_BUFFER_SIZE = MAX_CACHED_OBJECT_SIZE_MB*1024*1024;
  private static ThreadLocal<Kryo> kryoThreadLocal;
		  
			


  public KryoSerializer(ClassLoader classLoader) {
	  //kryo is not thread safe , we need to create a thread local for it
	  //, which creates new instances for new threads.
	  if(Objects.isNull(kryoThreadLocal)) {
		  kryoThreadLocal =  
				  new ThreadLocal<Kryo>() {
			        @Override
			        protected Kryo initialValue() {
			            return createKryoInstance(classLoader);
			        }
			    };
	  } 
  }

  
  
  
  @Override
  public ByteBuffer serialize(final Object object) throws SerializerException {
	Kryo kryo = kryoThreadLocal.get();
    Output output = new Output(SERIALIZE_BUFFER_SIZE);
    kryo.writeClassAndObject(output, object);
    return ByteBuffer.wrap(output.getBuffer());
  }

  
  
  
  @Override
  public Object read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
	Kryo kryo = kryoThreadLocal.get();
	Input input =  new Input(new ByteBufferInputStream(binary)) ;
	return kryo.readClassAndObject(input);
  }

  
  
  
  @Override
  public boolean equals(final Object object, final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    return object.equals(read(binary));
  }
  
  
  
  private Kryo createKryoInstance(ClassLoader classLoader) {
		Kryo kryo = new Kryo();
		   
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
	   
	   return kryo;
  }

}

