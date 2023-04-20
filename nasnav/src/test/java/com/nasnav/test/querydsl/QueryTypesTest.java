package com.nasnav.test.querydsl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.reflect.ClassPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.PathInits;

@SpringBootTest
public class QueryTypesTest {
  private static String SAMPLE_STRING = "sampleString";


  @Test
  public void queryTypeConstructors() throws IOException {
    ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses()
    .stream()
    .filter(clazz -> clazz.getPackageName()
      .startsWith("com.nasnav.querydsl")).forEach((classInfo) -> {
        Class<?> clazz = classInfo.load();
        AtomicReference<Constructor<?>> mainCtorRef = new AtomicReference<>(null);
        Map<Class<?>, Object> paramsMap = new HashMap<>();
        paramsMap.put(String.class, SAMPLE_STRING);
        paramsMap.put(PathInits.class, PathInits.DEFAULT);
        assertDoesNotThrow(() -> {
          Constructor<?> mainCtor = clazz.getConstructor(String.class);
          mainCtorRef.set(mainCtor);
          Path<?> path = (Path<?>) mainCtor.newInstance(paramsMap.get(String.class));
          paramsMap.put(Path.class, path);
          paramsMap.put(PathMetadata.class, path.getMetadata());

        });
        Constructor<?>[] constructors = classInfo.load().getConstructors();
          for(Constructor<?> ctor : constructors) {
            if (ctor != mainCtorRef.get()) {
              Class<?>[] paramTypes = ctor.getParameterTypes();

              if (Arrays.stream(paramTypes).allMatch(paramsMap::containsKey)) {
                Object[] params = Arrays.stream(paramTypes).map(paramsMap::get).toArray();
                assertDoesNotThrow(() -> {
                  ctor.newInstance(params);
                });
              }
            }
          }
      });
  }
}
