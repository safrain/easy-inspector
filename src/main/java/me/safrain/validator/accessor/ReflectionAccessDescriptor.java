package me.safrain.validator.accessor;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ReflectionAccessDescriptor {
    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    static ReflectionAccessDescriptor NULL_DESCRIPTOR = new ReflectionAccessDescriptor();
    Field field;
    Method method;
    String propertyName;

    private ReflectionAccessDescriptor() {
    }

    public ReflectionAccessDescriptor(List<Class<?>> classList, String propertyName) {
        this.propertyName = propertyName;
        // For raw object, check getter first, then check field
        String baseName;

        if (propertyName.length() > 1) {
            if (Character.isUpperCase(propertyName.charAt(1))) {
                baseName = propertyName;
            } else {
                baseName = propertyName.toUpperCase(Locale.ENGLISH) +
                        propertyName.substring(1);
            }
        } else {
            baseName = propertyName.toUpperCase(Locale.ENGLISH);
        }

        String getMethodName = GET_PREFIX + baseName;
        String isMethodName = IS_PREFIX + baseName;

        method = findGetter(classList, getMethodName);
        if (method == null) method = findGetter(classList, isMethodName);
        if (method == null) field = findField(classList, propertyName);
    }


    public static List<ReflectionAccessDescriptor> createDescriptorList(List<Class<?>> classList) {
        Map<String, Object> map = new HashMap<String, Object>();
        // Collect all fields
        for (Class<?> c : classList) {
            for (Field field : c.getDeclaredFields()) {
                if (!isPropertyField(field)) continue;
                if (map.containsKey(field.getName())) continue;
                map.put(field.getName(), field);
            }
        }

        // Collect all getters, getter have higher priority
        for (Class<?> c : classList) {
            for (Method method : c.getMethods()) {
                if (!isPropertyGetter(method)) continue;
                String methodName = method.getName();
                String baseName = null;
                if (methodName.startsWith(GET_PREFIX)) {
                    baseName = Introspector.decapitalize(method.getName().substring(GET_PREFIX.length()));
                } else if (methodName.startsWith(IS_PREFIX)) {
                    baseName = Introspector.decapitalize(method.getName().substring(IS_PREFIX.length()));
                }
                if (baseName == null) throw new IllegalStateException();
                Object existing = map.get(baseName);
                // Can overwrite field
                if (existing == null || existing instanceof Field) {
                    map.put(baseName, method);
                }
            }
        }

        // Transform fields/methods in the map to AccessDescriptors
        List<ReflectionAccessDescriptor> result = new ArrayList<ReflectionAccessDescriptor>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String propertyName = entry.getKey();
            Object v = entry.getValue();
            if (v instanceof Field) {
                ReflectionAccessDescriptor descriptor = new ReflectionAccessDescriptor();
                descriptor.propertyName = propertyName;
                descriptor.field = (Field) v;
                descriptor.field.setAccessible(true);
                result.add(descriptor);
            } else if (v instanceof Method) {
                ReflectionAccessDescriptor descriptor = new ReflectionAccessDescriptor();
                descriptor.propertyName = propertyName;
                descriptor.method = (Method) v;
                result.add(descriptor);
            }
        }
        return result;
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

    Field findField(List<Class<?>> classList, String fieldName) {
        for (Class<?> c : classList) {
            Field field;
            try {
                field = c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                continue;
            }
            if (!isPropertyField(field)) continue;
            field.setAccessible(true);
            return field;
        }
        return null;

    }

    Method findGetter(List<Class<?>> classList, String methodName) {
        for (Class<?> c : classList) {
            Method method;
            try {
                method = c.getMethod(methodName);
            } catch (NoSuchMethodException e) {
                continue;
            }
            if (!isPropertyGetter(method)) continue;
            return method;
        }
        return null;
    }

    static boolean isPropertyField(Field field) {
        if (Modifier.isStatic(field.getModifiers())) return false;
        return true;
    }

    static boolean isPropertyGetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) return false;
        if (method.getParameterTypes().length != 0) return false;
        if (method.getReturnType() == Void.class) return false;
        String methodName = method.getName();
        if (methodName.startsWith(IS_PREFIX)) {
            return method.getReturnType() == Boolean.TYPE;
        }
        if (methodName.startsWith(GET_PREFIX)) {
            return method.getReturnType() != Boolean.TYPE;
        }
        return false;
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