package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public class RootSegment implements PathSegment {
    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (context.isLast(index)) return context.checkValidation(object);
        return context.get(index + 1).process(object, index + 1, context, optional);
    }
}
