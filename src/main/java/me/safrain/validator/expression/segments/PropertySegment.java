
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public class PropertySegment implements PathSegment {
    String propertyName;

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.getPropertyAccessor().accept(object)) {
            return context.onRejected(object);
        }
        if (!context.getPropertyAccessor().accept(object, propertyName)) {
            return context.onRejected(null);
        }

        Object o = context.getPropertyAccessor().accessProperty(object, propertyName);
        if (context.isLastSegment(index)) return context.onValidation(o);


        PathSegment next = context.getSegment(index + 1);
        return next.process(o, index + 1, context, optional);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}