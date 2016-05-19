
package me.safrain.validator.expression.segments;

import me.safrain.validator.Violation;
import me.safrain.validator.expression.SegmentContext;

import java.util.Iterator;

public class AnyArrayElementSegment implements PathSegment {


    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.getArrayAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        int size = context.getArrayAccessor().size(object);
        context.suppressForce(this);
        try {
            boolean last = context.isLast(index);
            for (int i = 0; i < size; i++) {
                Object o = context.getArrayAccessor().accessIndex(object, i);
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