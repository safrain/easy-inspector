
package me.safrain.validator.expression.segments;

import me.safrain.validator.Violation;
import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.TransformingIterator;

public class AnyPropertySegment implements PathSegment {


    @Override
    public boolean process(final Object object, int index, final SegmentContext context, boolean optional) {
        if (!context.getPropertyAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        context.suppressForce(this);
        try {
            boolean last = context.isLast(index);
            for (String propertyName : context.getPropertyAccessor().getPropertyNames(object)) {
                Object o = context.getPropertyAccessor().accessProperty(object, propertyName);
                if ((last ?
                        context.checkValidation(o) :
                        context.get(index + 1).process(o, index + 1, context, optional))) {
                    return true;
                }
            }
        } finally {
            context.unsuppress(this);
        }
        if (!context.isViolationSuppressed()) {
            context.addViolation(new Violation("F"));
        }
        return false;
    }
}