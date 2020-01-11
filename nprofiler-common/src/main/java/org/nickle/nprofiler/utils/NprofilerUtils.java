package org.nickle.nprofiler.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class NprofilerUtils {
    public static void mapToObj(Map map, Object obj, Function mapValueResolve) {
        if (map == null || obj == null) {
            return;
        }
        Class<?> aClass = obj.getClass();
        try {
            Field[] declaredFields = aClass.getDeclaredFields();
            int length = declaredFields.length;
            for (int i = 0; i < length; i++) {
                Field field = declaredFields[i];
                field.setAccessible(true);
                MapToObjIgnore annotation = field.getAnnotation(MapToObjIgnore.class);
                if (annotation != null) {
                    continue;
                }
                String name = field.getName();
                Object value = map.get(name);
                if (value == null) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    value = map.get(name);
                    if (value != null) {
                        if (mapValueResolve != null) {
                            value = mapValueResolve.apply(value);
                        }
                        if (value != null) {
                            field.set(obj, value);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MapToObjIgnore {
    }
}
