package me.safrain.validator.accessor;

import java.util.List;

public interface PropertyAccessor {
    boolean accept(Object object);

    Object accessProperty(Object object, String propertyName);

    List<String> getPropertyNames(Object object);
}
