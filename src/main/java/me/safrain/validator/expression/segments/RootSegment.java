package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public class RootSegment implements PathSegment {
    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (context.isLastSegment(index)) return context.onValidation(object);
        return context.getSegment(index + 1).process(object, index + 1, context, optional);
    }
}
