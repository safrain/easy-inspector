package me.safrain.validator.expression;

import me.safrain.validator.ValidationContext;
import me.safrain.validator.Violation;
import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;

import java.lang.reflect.Method;

public class SegmentContext {
    public ValidationContext validationContext;
    public Method method;
    public Expression expression;
    public PropertyAccessor propertyAccessor;
    public ArrayAccessor arrayAccessor;
    public ValidateCommand validateCommand;

    public boolean hasNext(int index) {
        return index < expression.getSegments().size() - 1;
    }

    public boolean isObjectValid(Object object) {
        return object != null && object != ExpressionResolver.NOT_FOUND;
    }

    public boolean apply(Object object, int index, boolean optional) {
        // Check NOT_FOUND first
        if (object == ExpressionResolver.NOT_FOUND) {
            if (optional) {
                return true;
            } else {
                validationContext.violations.add(new Violation(method, expression));
                return false;
            }
        }

        // Has no more segments, call the validate function
        if (index >= expression.getSegments().size()) {

            // For null object & optional express, its success
            if (object == null && optional) return true;

            // Call the actual validate method
            try {
                boolean result = validateCommand.validate(object);
                if (!result) {
                    // Do not add violation while in manual mode
                    if (!validationContext.manual) {
                        validationContext.violations.add(new Violation(method, expression));
                    }
                    return false;
                }
                return true;
            } catch (Throwable e) {
                if (!validationContext.manual) {
                    validationContext.violations.add(new Violation(method, expression, e));
                }
                return false;
            }

        } else { // Call the segment, proceed to next segment
            PathSegment current = expression.getSegments().get(index);
            return current.process(object, index, this, optional);
        }
    }
}
