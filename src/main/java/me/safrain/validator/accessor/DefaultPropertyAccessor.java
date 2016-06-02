package me.safrain.validator.accessor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Locale.ENGLISH;

public class DefaultPropertyAccessor implements PropertyAccessor {
    Map<AccessDescriptor.Key, AccessDescriptor> cache = new ConcurrentHashMap<AccessDescriptor.Key, AccessDescriptor>();

    AccessDescriptor getAccessDescriptor(Object object, String propertyName) {
        AccessDescriptor.Key key = new AccessDescriptor.Key();
        key.type = object.getClass();
        key.propertyName = propertyName;

        AccessDescriptor descriptor = cache.get(key);
        if (descriptor == null) {
            descriptor = new AccessDescriptor(key.type, key.propertyName);
            if (descriptor.field == null || descriptor.method == null) descriptor = AccessDescriptor.NULL_DESCRIPTOR;
            cache.put(key, descriptor);
        }
        return descriptor != AccessDescriptor.NULL_DESCRIPTOR ? descriptor : null;
    }

    @Override
    public boolean acceptType(Object object) {
        return object instanceof Map || object != null;
    }


    @Override
    public boolean acceptAccess(Object object, String propertyName) {

        // For map, just check if the key exists
        if (object instanceof Map) {
            return ((Map) object).keySet().contains(propertyName);
        }
        return getAccessDescriptor(object, propertyName) != null;
    }


    @Override
    public Object accessProperty(Object object, String propertyName) {
        if (object instanceof Map) {
            return ((Map) object).get(propertyName);
        }
        return getAccessDescriptor(object, propertyName).access(object);
    }

    @Override
    public List<String> getPropertyNames(Object object) {
        List<String> result = new ArrayList<String>();
        for (Object o : ((Map) object).keySet()) {
            result.add((String) o);
        }
        return result;
    }

}

class AccessDescriptor {
    static AccessDescriptor NULL_DESCRIPTOR = new AccessDescriptor();
    Field field;
    Method method;

    private AccessDescriptor() {
    }

    public AccessDescriptor(Class<?> type, String propertyName) {
        // For raw object, check getter first, then check field
        String baseName = propertyName.substring(0, 1).toUpperCase(ENGLISH) + propertyName.substring(1);
        String getMethodName = "get" + baseName;
        String isMethodName = "is" + baseName;
        method = findGetter(type, getMethodName);
        if (method == null) method = findGetter(type, isMethodName);

        field = findField(type, propertyName);
    }

    public Object access(Object object) {
        if (field != null) {
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        if (method != null) {
            try {
                return method.invoke(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException();
    }

    Field findField(Class<?> clz, String fieldName) {
        for (Class<?> cl = clz; cl != null; cl = cl.getSuperclass()) {
            Field field;
            try {
                field = cl.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            return field;
        }
        return null;

    }

    Method findGetter(Class<?> clz, String methodName) {
        for (Class<?> cl = clz; cl != null; cl = cl.getSuperclass()) {
            Method method;
            try {
                method = cl.getMethod(methodName);
            } catch (NoSuchMethodException e) {
                continue;
            }
            if (method.getParameterTypes().length != 0) continue;
            if (method.getReturnType() == Boolean.TYPE && !methodName.startsWith("is")) continue;
            if (method.getReturnType() != Boolean.TYPE && !methodName.startsWith("get")) continue;
            return method;
        }
        return null;
    }

    static class Key {
        Class<?> type;
        String propertyName;

        @Override
        public int hashCode() {
            // Both good hashed, so xor should be enough
            return type.hashCode() ^ propertyName.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (type != null ? !type.equals(key.type) : key.type != null) return false;
            return propertyName != null ? propertyName.equals(key.propertyName) : key.propertyName == null;
        }
    }
}
