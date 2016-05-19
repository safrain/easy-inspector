package me.safrain.validator.accessor;

import java.util.List;

public interface PropertyAccessor {
    boolean accept(Object object);

    boolean accept(Object object, String propertyName);

    Object accessProperty(Object object, String propertyName);

    List<String> getPropertyNames(Object object);
}
