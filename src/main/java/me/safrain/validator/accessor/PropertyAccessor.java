package me.safrain.validator.accessor;

import java.util.List;

public interface PropertyAccessor {
    boolean acceptType(Object object);

    boolean acceptAccess(Object object, String propertyName);

    Object accessProperty(Object object, String propertyName);

    List<String> getPropertyNames(Object object);
}
