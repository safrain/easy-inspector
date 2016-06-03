package me.safrain.validator.accessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPropertyAccessor implements PropertyAccessor {
    private Map<ReflectionAccessDescriptor.Key, ReflectionAccessDescriptor> accessCache = new ConcurrentHashMap<ReflectionAccessDescriptor.Key, ReflectionAccessDescriptor>();
    private Map<Class<?>, List<String>> listNameCache = new ConcurrentHashMap<Class<?>, List<String>>();

    private List<String> omittedPackages = Arrays.asList("java", "javax", "sun", "jdk");

    private ReflectionAccessDescriptor getAccessDescriptor(Object object, String propertyName) {
        ReflectionAccessDescriptor.Key key = new ReflectionAccessDescriptor.Key();
        key.type = object.getClass();
        key.propertyName = propertyName;

        ReflectionAccessDescriptor descriptor = accessCache.get(key);
        if (descriptor == null) {
            descriptor = new ReflectionAccessDescriptor(getViableClassList(key.type), key.propertyName);
            if (descriptor.field == null && descriptor.method == null)
                descriptor = ReflectionAccessDescriptor.NULL_DESCRIPTOR;
            accessCache.put(key, descriptor);
        }
        return descriptor != ReflectionAccessDescriptor.NULL_DESCRIPTOR ? descriptor : null;
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
        // For raw object, check if a descriptor exists
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
        if (object instanceof Map) {
            List<String> result = new ArrayList<String>();
            for (Object o : ((Map) object).keySet()) {
                result.add((String) o);
            }
            return result;
        }

        Class<?> type = object.getClass();
        List<String> result = listNameCache.get(type);
        if (result == null) {
            result = new ArrayList<String>();
            for (ReflectionAccessDescriptor descriptor : ReflectionAccessDescriptor.createDescriptorList(getViableClassList(object.getClass()))) {
                // TODO filter unwanted properties here
                ReflectionAccessDescriptor.Key key = new ReflectionAccessDescriptor.Key();
                key.type = type;
                key.propertyName = descriptor.propertyName;
                accessCache.put(key, descriptor);
                result.add(descriptor.propertyName);
            }
            listNameCache.put(type, result);
        }
        return result;
    }

    private List<Class<?>> getViableClassList(Class<?> type) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (Class<?> c = type; c != null && !isOmittedClass(c); c = c.getSuperclass()) {
            result.add(c);
        }
        return result;
    }

    private boolean isOmittedClass(Class<?> type) {
        if (type.isPrimitive()) return true;
        String packageName = type.getPackage().getName();
        for (String p : omittedPackages) {
            if (packageName.equals(p) || packageName.startsWith(p + ".")) return true;
        }
        return false;
    }

    public List<String> getOmittedPackages() {
        return omittedPackages;
    }

    public void setOmittedPackages(List<String> omittedPackages) {
        this.omittedPackages = omittedPackages;
    }
}

