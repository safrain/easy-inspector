
package me.safrain.validator.expression;

public class ArrayIndexAccessSegment implements PathSegment {
    int arrayIndex;

    public ArrayIndexAccessSegment(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public static ArrayIndexAccessSegment parse(String segment) {
        return new ArrayIndexAccessSegment(Integer.valueOf(segment));
    }

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (object == null) return context.apply(ExpressionResolver.NOT_FOUND, index + 1, optional);

        if (!context.arrayAccessor.accept(object, arrayIndex)) {
            return context.apply(ExpressionResolver.NOT_FOUND, index + 1, optional);
        }
        Object o = context.arrayAccessor.accessIndex(object, arrayIndex);
        return context.apply(o, index + 1, optional);
    }

}