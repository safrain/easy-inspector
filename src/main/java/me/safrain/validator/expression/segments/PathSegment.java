package me.safrain.validator.expression.segments;

import me.safrain.validator.expression.SegmentContext;

public interface PathSegment {

    boolean process(Object object, int index, SegmentContext context, boolean optional);

}