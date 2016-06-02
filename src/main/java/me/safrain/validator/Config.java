package me.safrain.validator;

import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;

public class Config {
    private PropertyAccessor propertyAccessor;
    private ArrayAccessor arrayAccessor;

    public PropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public void setPropertyAccessor(PropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
    }

    public ArrayAccessor getArrayAccessor() {
        return arrayAccessor;
    }

    public void setArrayAccessor(ArrayAccessor arrayAccessor) {
        this.arrayAccessor = arrayAccessor;
    }
}
