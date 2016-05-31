
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public class ArrayIndexAccessSegment implements PathSegment {
    int arrayIndex;

    public ArrayIndexAccessSegment(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.getArrayAccessor().accept(object)) {
            return context.onRejected(object);
        }
        if (!context.getArrayAccessor().accept(object, arrayIndex)) {
            return context.onRejected(null);
        }


        Object o = context.getArrayAccessor().accessIndex(object, arrayIndex);
        if (context.isLastSegment(index)) {
            return context.onValidation(o);
        } else {
            return context.getSegment(index + 1).process(o, index, context, optional);
        }
    }


}