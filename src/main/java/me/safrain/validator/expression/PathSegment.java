package me.safrain.validator.expression;

public interface PathSegment {

    boolean process(Object object, int index, SegmentContext context, boolean optional);

}