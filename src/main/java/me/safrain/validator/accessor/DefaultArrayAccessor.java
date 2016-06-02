package me.safrain.validator.accessor;

import java.lang.reflect.Array;
import java.util.List;

public class DefaultArrayAccessor implements ArrayAccessor {
    @Override
    public boolean acceptAccess(Object object, int index) {
        if (object == null) return false;
        if (!(object instanceof List) &&
                !(object.getClass().isArray())) {
            return false;
        }
        int size = size(object);
        if (index >= size) return false;
        if (index < -size) return false;
        return true;
    }

    @Override
    public boolean acceptType(Object object) {
        if (object == null) return false;
        if (object instanceof List) return true;
        if (object.getClass().isArray()) return true;
        return false;
    }

    @Override
    public int size(Object object) {
        if (object instanceof List) {
            return ((List) object).size();
        }
        if (object.getClass().isArray()) {
            return Array.getLength(object);
        }
        throw new IllegalStateException();
    }

    @Override
    public Object accessIndex(Object object, int index) {
        if (object instanceof List) {
            int size = ((List) object).size();
            return ((List) object).get(index < 0 ? size + index : index);
        }
        if (object.getClass().isArray()) {
            int size = Array.getLength(object);
            return Array.get(object, index < 0 ? size + index : index);
        }

        throw new IllegalStateException();
    }
}
