package me.safrain.validator.expression;

import javafx.scene.shape.Path;
import me.safrain.validator.ValidationContext;
import me.safrain.validator.Violation;
import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;
import me.safrain.validator.expression.resolver.ExpressionResolver;
import me.safrain.validator.expression.segments.PathSegment;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Stack;

public class SegmentContext {
    private ValidationContext validationContext;
    private Method method;
    private Expression expression;
    private PropertyAccessor propertyAccessor;
    private ArrayAccessor arrayAccessor;
    private ValidateCommand validateCommand;
    private Object[] args;

    private Stack<Boolean> suppressStack = new Stack<Boolean>();

    public void notFound() {
        addViolation(new Violation(method, expression));
    }

    public boolean checkNullOptional(Object object) {
        if (object == null && expression.isOptional()) {
            return true;
        } else {
            addViolation(new Violation(method, expression));
            return false;
        }
    }


    public boolean hasNextSegment(int index) {
        return index < expression.getSegments().size() - 1;
    }

    public boolean isObjectValid(Object object) {
        return object != null && object != ExpressionResolver.NOT_FOUND;
    }

    public boolean applyWithoutViolation(Object object, int index, boolean optional) {
        return apply(object, index, optional, false);
    }

    public boolean applyWithViolation(Object object, int index, boolean optional) {
        return apply(object, index, optional, true);
    }

    public boolean isLast(int index) {
        return index == validationContext.scope.size() + expression.getSegments().size() - 1;
    }

    public PathSegment get(int index) {
        return index < validationContext.scope.size() ?
                validationContext.scope.get(index) :
                expression.getSegments().get(index - validationContext.scope.size());
    }

    public boolean checkValidation(Object object) {
        // Call the actual validate method
        try {
            if (!validateCommand.validate(object)) {
                addViolation(new Violation(method, expression, object));
                return false;
            }
            return true;
        } catch (Throwable e) {
            addViolation(new Violation(method, expression, object, e));
            return false;
        }
    }

    public boolean apply(Object object, int index, boolean optional, boolean withViolation) {
        // Check NOT_FOUND first
        if (object == ExpressionResolver.NOT_FOUND) {
            if (optional) {
                return true;
            } else {
                if (withViolation) addViolation(new Violation(method, expression, object));
                return false;
            }
        }

        // Has no more segments, call the validate function
        if (index >= validationContext.scope.size() + expression.getSegments().size()) {

            // For null object & optional express, its success
            if (object == null && optional) return true;

            // Call the actual validate method
            try {
                if (!validateCommand.validate(object)) {
                    if (withViolation) addViolation(new Violation(method, expression, object));
                    return false;
                }
                return true;
            } catch (Throwable e) {
                if (withViolation) addViolation(new Violation(method, expression, object, e));
                return false;
            }

        } else {
            // Call the segment, proceed to next segment
            PathSegment current = index < validationContext.scope.size() ?
                    validationContext.scope.get(index) :
                    expression.getSegments().get(index - validationContext.scope.size());
            return current.process(object, index, this, optional);
        }
    }

    public void addViolation(Violation violation) {
        if (!isViolationSuppressed()) {
            // Do not add violation while in manual mode or already has violation in aggregate sibling
            if (args != null) {
                Object[] a = new Object[args.length - 1];
                System.arraycopy(args, 1, a, 0, args.length - 1);
                violation.setArgs(a);
            }
            validationContext.violations.add(violation);
        }
    }

    public boolean isViolationSuppressed() {
        return validationContext.manual || suppress;
    }


    PathSegment suppressSegment;
    boolean suppress;

    public void suppress(PathSegment segment) {
        if (suppressSegment != null) return;
        suppressSegment = segment;
        suppress = false;
    }

    public void suppressForce(PathSegment segment) {
        if (suppressSegment != null) return;
        suppressSegment = segment;
        suppress = true;
    }

    public void unsuppress(PathSegment segment) {
        if (suppressSegment != segment) return;
        suppressSegment = null;
        suppress = false;
    }

    // Used to called by iterative segments
    public boolean iterativeApplyEvery(Object object, int index, boolean optional, Iterator iterator) {
        boolean result = true;
        // In a aggregate segment, violations in each segment should be collect only once
        // Use this stack to prevent 'Too many violations' case happens
        // (Consider each element in a huge array produces a violation)
        if (suppressStack.isEmpty()) {
            suppressStack.push(false);
        } else {
            // Copy stack top to propagate suppress flag
            suppressStack.push(suppressStack.peek());
        }

        // Use the given iterator to do the iteration process
        while (iterator.hasNext()) {
            Object value = iterator.next();
            boolean r = applyWithViolation(value, index, optional && !(isObjectValid(object) && !hasNextSegment(index)));
            result &= r;

            // Set flag on the stack top while an element is invalid
            if (!r && !suppressStack.peek()) {
                suppressStack.set(suppressStack.size() - 1, true);
            }
        }

        suppressStack.pop();

        return result;
    }

    public boolean iterativeApplyAny(Object object, int index, boolean optional, Iterator iterator) {
        // No violation should be added in 'any', just force suppress
        suppressStack.push(true);
        while (iterator.hasNext()) {
            Object value = iterator.next();
            boolean r = applyWithViolation(value, index, optional && !(isObjectValid(object) && !hasNextSegment(index)));
            if (r) return true;
        }
        suppressStack.pop();
        addViolation(new Violation("Not found any"));
        return false;
    }


    public ValidationContext getValidationContext() {
        return validationContext;
    }

    public void setValidationContext(ValidationContext validationContext) {
        this.validationContext = validationContext;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public PropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public void setPropertyAccessor(PropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
    }

    public ArrayAccessor getArrayAccessor() {
        return arrayAccessor;
    }

    public void setArrayAccessor(ArrayAccessor arrayAccessor) {
        this.arrayAccessor = arrayAccessor;
    }

    public ValidateCommand getValidateCommand() {
        return validateCommand;
    }

    public void setValidateCommand(ValidateCommand validateCommand) {
        this.validateCommand = validateCommand;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
