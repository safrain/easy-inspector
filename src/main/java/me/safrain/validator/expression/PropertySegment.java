
package me.safrain.validator.expression;

public class PropertySegment implements PathSegment {
    String propertyName;

    public static PropertySegment parse(String segment) {
        PropertySegment result = new PropertySegment();
        result.propertyName = segment;
        return result;
    }

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.propertyAccessor.accept(object)) {
            return context.apply(ExpressionResolver.NOT_FOUND, index + 1, optional);
        }
        Object o = context.propertyAccessor.accessProperty(object, propertyName);
        return context.apply(o, index + 1, optional);
    }

}