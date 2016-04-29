package me.safrain.validator.accessor;

public interface ArrayAccessor {
    boolean accept(Object object, int index);

    boolean accept(Object object);

    int size(Object object);

    Object accessIndex(Object object, int index);
}
