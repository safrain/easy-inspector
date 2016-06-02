
package me.safrain.validator.expression.segments;

import me.safrain.validator.Violation;
import me.safrain.validator.expression.SegmentContext;

public class AnyArrayElementSegment implements PathSegment {


    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.getArrayAccessor().acceptType(object)) {
            return context.onRejected(object);
        }
        int size = context.getArrayAccessor().size(object);
        context.activateSuppressMode(this, true);
        try {
            boolean last = context.isLastSegment(index);
            for (int i = 0; i < size; i++) {
                Object o = context.getArrayAccessor().accessIndex(object, i);
                if ((last ?
                        context.onValidation(o) :
                        context.getSegment(index + 1).process(o, index + 1, context, optional))) {
                    return true;
                }
            }
        } finally {
            context.deactivateSuppressMode(this);
        }

        if (!context.isViolationSuppressed()) {
            context.addViolation(new Violation("Not found"));
        }
        return false;
    }
}