package me.safrain.validator.accessor;

public interface ArrayAccessor {
    boolean acceptAccess(Object object, int index);

    boolean acceptType(Object object);

    int size(Object object);

    Object accessIndex(Object object, int index);
}
