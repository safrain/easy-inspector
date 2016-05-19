
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public class PropertySegment implements PathSegment {
    String propertyName;

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.getPropertyAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        if (!context.getPropertyAccessor().accept(object, propertyName)) {
            return context.checkNullOptional(null);
        }

        Object o = context.getPropertyAccessor().accessProperty(object, propertyName);
        if (context.isLast(index)) return context.checkValidation(o);


        PathSegment next = context.get(index + 1);
        return next.process(o, index + 1, context, optional);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}