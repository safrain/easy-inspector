package me.safrain.validator.accessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPropertyAccessor implements PropertyAccessor {
    Map<ReflectionAccessDescriptor.Key, ReflectionAccessDescriptor> accessCache = new ConcurrentHashMap<ReflectionAccessDescriptor.Key, ReflectionAccessDescriptor>();
    Map<Class<?>, List<String>> listNameCache = new ConcurrentHashMap<Class<?>, List<String>>();

    ReflectionAccessDescriptor getAccessDescriptor(Object object, String propertyName) {
        ReflectionAccessDescriptor.Key key = new ReflectionAccessDescriptor.Key();
        key.type = object.getClass();
        key.propertyName = propertyName;

        ReflectionAccessDescriptor descriptor = accessCache.get(key);
        if (descriptor == null) {
            descriptor = new ReflectionAccessDescriptor(key.type, key.propertyName);
            if (descriptor.field == null || descriptor.method == null) descriptor = ReflectionAccessDescriptor.NULL_DESCRIPTOR;
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
            for (ReflectionAccessDescriptor descriptor : ReflectionAccessDescriptor.createDescriptorList(object.getClass())) {
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
}

