
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

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
            return context.onRejected(object);
        }
        int size = context.getArrayAccessor().size(object);

        int actualIndexFrom = arrayIndexFrom > 0 ? arrayIndexFrom : size - arrayIndexFrom;
        int actualIndexTo = arrayIndexTo > 0 ? arrayIndexTo : size + arrayIndexTo;

        // Check out of bound
        if (!context.getArrayAccessor().accept(object, actualIndexFrom) || !context.getArrayAccessor().accept(object, actualIndexTo)) {
            return context.onRejected(null);
        }


        boolean reverse = actualIndexTo < actualIndexFrom;

        context.activateSuppressMode(this, false);
        try {
            boolean last = context.isLastSegment(index);
            for (int i = actualIndexFrom;
                 reverse ? (i > actualIndexTo) : (i < actualIndexTo);
                 i += reverse ? -1 : 1) {
                Object o = context.getArrayAccessor().accessIndex(object, i);
                if (!(last ?
                        context.onValidation(o) :
                        context.getSegment(index + 1).process(o, index + 1, context, optional))) {
                    return false;
                }
            }
        } finally {
            context.deactivateSuppressMode(this);
        }
        return false;
    }

}