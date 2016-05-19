
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.resolver.ExpressionResolver;

import java.util.Iterator;

public class ArrayRangeAccessSegment implements PathSegment {

    private int arrayIndexFrom;
    private int arrayIndexTo;

    public ArrayRangeAccessSegment(int arrayIndexFrom, int arrayIndexTo) {
        this.arrayIndexFrom = arrayIndexFrom;
        this.arrayIndexTo = arrayIndexTo;
    }

    @Override
    public boolean process(final Object object, int index, final SegmentContext context, boolean optional) {
        if (!context.getArrayAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        int size = context.getArrayAccessor().size(object);

        int actualIndexFrom = arrayIndexFrom > 0 ? arrayIndexFrom : size - arrayIndexFrom;
        int actualIndexTo = arrayIndexTo > 0 ? arrayIndexTo : size + arrayIndexTo;

        boolean reverse = actualIndexTo < actualIndexFrom;

        context.suppress(this);
        try {
            boolean last = context.isLast(index);
            for (int i = actualIndexFrom;
                 reverse ? (i > actualIndexTo) : (i < actualIndexTo);
                 i += reverse ? -1 : 1) {
                Object o = context.getArrayAccessor().accessIndex(object, i);
                if (!(last ?
                        context.checkValidation(o) :
                        context.get(index + 1).process(o, index + 1, context, optional))) {
                    return false;
                }
            }
        } finally {
            context.unsuppress(this);
        }
        return false;
    }

}