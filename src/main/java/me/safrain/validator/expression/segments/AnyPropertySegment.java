
package me.safrain.validator.expression.segments;

import me.safrain.validator.Violation;
import me.safrain.validator.expression.SegmentContext;

public class AnyPropertySegment implements PathSegment {


    @Override
    public boolean process(final Object object, int index, final SegmentContext context, boolean optional) {
        if (!context.getPropertyAccessor().acceptType(object)) {
            return context.onRejected(object);
        }
        context.activateSuppressMode(this, true);
        try {
            boolean last = context.isLastSegment(index);
            for (String propertyName : context.getPropertyAccessor().getPropertyNames(object)) {
                Object o = context.getPropertyAccessor().accessProperty(object, propertyName);
                if ((last ?
                        context.onValidation(o) :
                        context.getSegment(index + 1).process(o, index + 1, context, optional))) {
                    return true;
                }
            }
        } finally {
            context.deactivateSuppressMode(this);
        }
        if (!context.isViolationSuppressed()) {
            Violation violation = context.createViolation();
            violation.setMessage("Not found in object");
            violation.setType(Violation.Type.INVALID);
            context.addViolation(violation);
        }
        return false;
    }
}