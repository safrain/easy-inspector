
package me.safrain.validator.expression.segments;

import me.safrain.validator.Violation;
import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.TransformingIterator;
import me.safrain.validator.expression.resolver.ExpressionResolver;

import java.util.List;

public class EveryPropertySegment implements PathSegment {

    @Override
    public boolean process(final Object object, int index, final SegmentContext context, boolean optional) {
        if (!context.getPropertyAccessor().accept(object)) {
            return context.checkNullOptional(object);
        }
        context.suppress(this);
        try {
            boolean last = context.isLast(index);
            for (String propertyName : context.getPropertyAccessor().getPropertyNames(object)) {
                Object o = context.getPropertyAccessor().accessProperty(object, propertyName);
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