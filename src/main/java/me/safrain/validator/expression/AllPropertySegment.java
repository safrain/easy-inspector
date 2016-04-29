
package me.safrain.validator.expression;

import java.util.List;

public class AllPropertySegment implements PathSegment {
    public static AllPropertySegment parse(String segment) {
        if ("*".equals(segment)) return new AllPropertySegment();
        return null;
    }

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.propertyAccessor.accept(object)) {
            return context.apply(ExpressionResolver.NOT_FOUND, index + 1, optional);
        }
        List<String> propertyNames = context.propertyAccessor.getPropertyNames(object);
        boolean r = true;
        for (String propertyName : propertyNames) {
            Object value = context.propertyAccessor.accessProperty(object, propertyName);
            r = r & context.apply(value, index + 1,
                    optional && !(
                            context.isObjectValid(object) &&
                                    !context.hasNext(index))
            );
        }

        return r;
    }
}