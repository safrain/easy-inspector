
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.resolver.ExpressionResolver;

public class ArrayIndexAccessSegment implements PathSegment {
    int arrayIndex;

    public ArrayIndexAccessSegment(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.getArrayAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        if (!context.getArrayAccessor().accept(object, arrayIndex)) {
            return context.checkNullOptional(null);
        }


        Object o = context.getArrayAccessor().accessIndex(object, arrayIndex);
        if (context.isLast(index)) {
            return context.checkValidation(o);
        } else {
            return context.get(index + 1).process(o, index, context, optional);
        }
    }


}