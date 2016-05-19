
package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public class EveryArrayElementSegment implements PathSegment {


    @Override
    public boolean process(final Object object, int index, final SegmentContext context, boolean optional) {
        if (!context.getArrayAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        int size = context.getArrayAccessor().size(object);

        //
        context.suppress(this);
        try {
            boolean last = context.isLast(index);
            for (int i = 0; i < size; i++) {
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
        return true;
    }
}