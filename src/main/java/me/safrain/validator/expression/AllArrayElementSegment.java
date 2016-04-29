
package me.safrain.validator.expression;

public class AllArrayElementSegment implements PathSegment {

    public static AllArrayElementSegment parse() {
        AllArrayElementSegment result = new AllArrayElementSegment();
        return result;
    }

    @Override
    public boolean process(Object object, int index, SegmentContext context, boolean optional) {
        if (!context.arrayAccessor.accept(object)) {
            return context.apply(ExpressionResolver.NOT_FOUND, index + 1, optional);
        }
        int size = context.arrayAccessor.size(object);
        boolean r = true;
        for (int i = 0; i < size; i++) {
            Object element = context.arrayAccessor.accessIndex(object, i);
            r = r & context.apply(element, index + 1, optional && !(
                    context.isObjectValid(object) &&
                            !context.hasNext(index)));
        }

        return r;
    }

}