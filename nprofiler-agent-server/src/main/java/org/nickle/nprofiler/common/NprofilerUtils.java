package org.nickle.nprofiler.common;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class NprofilerUtils {
    public static void mapToObj(Map map, Object obj, Function mapValueResolve) {
        if (map == null || obj == null) {
            return;
        }
        Set<Map.Entry> set = map.entrySet();
        Iterator<Map.Entry> iterator = set.iterator();
        Class<?> aClass = obj.getClass();
        try {
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                Object key = entry.getKey();
                if (key instanceof String) {
                    String keyStr = (String) key;
                    Field field = aClass.getDeclaredField(keyStr);
                    if (field != null) {
                        field.setAccessible(true);
                        MapToObjIgnore annotation = field.getAnnotation(MapToObjIgnore.class);
                        if (annotation != null) {
                            Object value = entry.getValue();
                            if (mapValueResolve != null) {
                                value = mapValueResolve.apply(value);
                            }
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
